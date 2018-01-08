package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeRequestedAfterDeadlineException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final JsonMapper jsonMapper;
    private final IssueDateCalculator issueDateCalculator;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final UserService userService;
    private final EventProducer eventProducer;
    private final InterestCalculationService interestCalculationService;

    @Autowired
    public ClaimService(
        ClaimRepository claimRepository,
        UserService userService,
        JsonMapper jsonMapper,
        IssueDateCalculator issueDateCalculator,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        EventProducer eventProducer,
        InterestCalculationService interestCalculationService) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.jsonMapper = jsonMapper;
        this.issueDateCalculator = issueDateCalculator;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.eventProducer = eventProducer;
        this.interestCalculationService = interestCalculationService;
    }

    public Claim getClaimById(long claimId) {
        return calculateAndPopulateTotalAmount(
            claimRepository
                .getById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found by id " + claimId))
        );
    }

    public List<Claim> getClaimBySubmitterId(String submitterId) {
        return claimRepository.getBySubmitterId(submitterId).stream()
            .map((claim) -> calculateAndPopulateTotalAmount(claim)).collect(Collectors.toList());
    }

    public Claim getClaimByLetterHolderId(String id) {
        return calculateAndPopulateTotalAmount(
            claimRepository
                .getByLetterHolderId(id)
                .orElseThrow(() -> new NotFoundException("Claim not found for letter holder id " + id))
        );
    }

    public Claim getClaimByExternalId(String externalId) {
        return calculateAndPopulateTotalAmount(
            claimRepository
                .getClaimByExternalId(externalId)
                .orElseThrow(() -> new NotFoundException("Claim not found by external id " + externalId))
        );
    }

    public Optional<Claim> getClaimByReference(String reference, String authorisation) {
        String submitterId = userService.getUserDetails(authorisation).getId();

        Optional<Claim> optionalClaim = claimRepository.getByClaimReferenceAndSubmitter(reference, submitterId);

        if (optionalClaim.isPresent()) {
            Claim claim = optionalClaim.get();
            optionalClaim = Optional.of(calculateAndPopulateTotalAmount(claim));
        }

        return optionalClaim;
    }

    public Optional<Claim> getClaimByReference(String reference) {

        Optional<Claim> optionalClaim = claimRepository.getByClaimReferenceNumber(reference);

        if (optionalClaim.isPresent()) {
            Claim claim = optionalClaim.get();
            optionalClaim = Optional.of(calculateAndPopulateTotalAmount(claim));
        }

        return optionalClaim;
    }

    public List<Claim> getClaimByExternalReference(String externalReference, String authorisation) {
        String submitterId = userService.getUserDetails(authorisation).getId();
        return claimRepository.getByExternalReference(externalReference, submitterId)
            .stream().map((claim) -> calculateAndPopulateTotalAmount(claim)).collect(Collectors.toList());
    }

    public List<Claim> getClaimByDefendantId(String id) {
        return claimRepository.getByDefendantId(id);
    }

    @Transactional
    public Claim saveClaim(String submitterId, ClaimData claimData, String authorisation) {
        String externalId = claimData.getExternalId().toString();

        claimRepository.getClaimByExternalId(externalId).ifPresent(claim -> {
            throw new ConflictException("Duplicate claim for external id " + claim.getExternalId());
        });

        LocalDateTime now = LocalDateTimeFactory.nowInLocalZone();
        Optional<GeneratePinResponse> pinResponse = Optional.empty();

        if (!claimData.isClaimantRepresented()) {
            pinResponse = Optional.of(userService.generatePin(claimData.getDefendant().getName(), authorisation));
        }

        Optional<String> letterHolderId = pinResponse.map(GeneratePinResponse::getUserId);
        LocalDate issuedOn = issueDateCalculator.calculateIssueDay(now);
        LocalDate responseDeadline = responseDeadlineCalculator.calculateResponseDeadline(issuedOn);
        UserDetails userDetails = userService.getUserDetails(authorisation);
        String submitterEmail = userDetails.getEmail();

        String claimDataString = jsonMapper.toJson(claimData);

        long issuedClaimId;

        if (claimData.isClaimantRepresented()) {
            issuedClaimId = claimRepository.saveRepresented(claimDataString, submitterId, issuedOn,
                responseDeadline, externalId, submitterEmail);
        } else {
            issuedClaimId = claimRepository.saveSubmittedByClaimant(claimDataString, submitterId,
                letterHolderId.orElseThrow(IllegalStateException::new), issuedOn, responseDeadline,
                externalId, submitterEmail);
        }

        eventProducer.createClaimIssuedEvent(
            getClaimById(issuedClaimId),
            pinResponse.map(GeneratePinResponse::getPin).orElse(null),
            userDetails.getFullName(),
            authorisation
        );

        return getClaimById(issuedClaimId);
    }

    public Claim requestMoreTimeForResponse(long claimId, String authorisation) {
        UserDetails defendant = userService.getUserDetails(authorisation);
        Claim claim = getClaimById(claimId);

        if (!claim.getDefendantId()
            .equals(defendant.getId())) {
            throw new ForbiddenActionException("This claim is not raised against you");
        }

        if (claim.isMoreTimeRequested()) {
            throw new MoreTimeAlreadyRequestedException("You have already requested more time");
        }

        if (LocalDate.now().isAfter(claim.getResponseDeadline())) {
            throw new MoreTimeRequestedAfterDeadlineException("You must not request more time after deadline");
        }

        LocalDate newDeadline = responseDeadlineCalculator
            .calculatePostponedResponseDeadline(claim.getIssuedOn());

        claimRepository.requestMoreTime(claimId, newDeadline);
        claim = getClaimById(claimId);
        eventProducer.createMoreTimeForResponseRequestedEvent(claim, newDeadline, defendant.getEmail());

        return claim;
    }

    public void linkDefendantToClaim(Long claimId, String defendantId) {
        Claim claim = claimRepository.getById(claimId)
            .orElseThrow(() -> new NotFoundException("Claim not found by id: " + claimId));

        claimRepository.linkDefendant(claim.getId(), defendantId);
    }

    public void linkLetterHolder(Long claimId, String userId) {
        claimRepository.linkLetterHolder(claimId, userId);
    }

    public void linkSealedClaimDocument(Long claimId, String documentSelfPath) {
        claimRepository.linkSealedClaimDocument(claimId, documentSelfPath);
    }

    public void saveCountyCourtJudgment(long claimId, CountyCourtJudgment countyCourtJudgment) {
        claimRepository.saveCountyCourtJudgment(claimId, jsonMapper.toJson(countyCourtJudgment));
    }

    public void saveDefendantResponse(long claimId, String defendantId, String defendantEmail, Response response) {
        claimRepository.saveDefendantResponse(claimId, defendantId, defendantEmail, jsonMapper.toJson(response));
    }

    private Claim calculateAndPopulateTotalAmount(Claim claim) {
        ClaimData data = claim.getClaimData();

        if (data.getAmount() instanceof AmountBreakDown) {
            BigDecimal claimAmount = ((AmountBreakDown) data.getAmount()).getTotalAmount();

            if (data.getInterest().getType() != Interest.InterestType.NO_INTEREST) {
                return claimWithInterest(claim, data, claimAmount);
            } else {
                return claim.copy(
                    claimAmount.add(data.getFeesPaidInPound()),
                    claimAmount.add(data.getFeesPaidInPound())
                );
            }
        }

        return claim;
    }

    private Claim claimWithInterest(Claim claim, ClaimData data, BigDecimal claimAmount) {
        BigDecimal rate = data.getInterest().getRate();
        LocalDate fromDate = getStartingDate(claim);
        BigDecimal interestTillToday = interestCalculationService.calculateInterestUpToNow(
            claimAmount, rate, fromDate
        );
        BigDecimal interestTillDateOfIssue = interestCalculationService.calculateInterest(
            claimAmount, rate, fromDate, claim.getCreatedAt().toLocalDate()
        );

        return claim.copy(
            interestTillToday.add(claimAmount).add(data.getFeesPaidInPound()),
            interestTillDateOfIssue.add(claimAmount).add(data.getFeesPaidInPound())
        );
    }

    private LocalDate getStartingDate(Claim claim) {
        if (claim.getClaimData().getInterestDate().getType() == InterestDate.InterestDateType.SUBMISSION) {
            return claim.getCreatedAt().toLocalDate();
        }

        return claim.getClaimData().getInterestDate().getDate();
    }
}
