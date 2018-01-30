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
import uk.gov.hmcts.cmc.claimstore.services.search.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final JsonMapper jsonMapper;
    private final IssueDateCalculator issueDateCalculator;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final UserService userService;
    private final EventProducer eventProducer;
    private final CaseRepository caseRepository;

    @Autowired
    public ClaimService(
        ClaimRepository claimRepository,
        UserService userService,
        JsonMapper jsonMapper,
        IssueDateCalculator issueDateCalculator,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        EventProducer eventProducer,
        CaseRepository caseRepository
    ) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.jsonMapper = jsonMapper;
        this.issueDateCalculator = issueDateCalculator;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.eventProducer = eventProducer;
        this.caseRepository = caseRepository;
    }

    public Claim getClaimById(long claimId) {
        return claimRepository
            .getById(claimId)
            .orElseThrow(() -> new NotFoundException("Claim not found by id " + claimId));
    }

    public List<Claim> getClaimBySubmitterId(String submitterId, String authorisation) {
        return caseRepository.getBySubmitterId(submitterId, authorisation);
    }

    public Claim getClaimByLetterHolderId(String id) {
        return claimRepository
            .getByLetterHolderId(id)
            .orElseThrow(() -> new NotFoundException("Claim not found for letter holder id " + id));
    }

    public Claim getClaimByExternalId(String externalId, String authorisation) {
        return caseRepository
            .getClaimByExternalId(externalId, authorisation)
            .orElseThrow(() -> new NotFoundException("Claim not found by external id " + externalId));
    }

    public Optional<Claim> getClaimByReference(String reference, String authorisation) {
        return caseRepository
            .getByClaimReferenceNumber(reference, authorisation);
    }

    public Optional<Claim> getClaimByReferenceAnonymous(String reference) {
        return claimRepository
            .getByClaimReferenceNumberAnonymous(reference);
    }

    public List<Claim> getClaimByExternalReference(String externalReference, String authorisation) {
        String submitterId = userService.getUserDetails(authorisation).getId();
        return claimRepository.getByExternalReference(externalReference, submitterId);
    }

    public List<Claim> getClaimByDefendantId(String id) {
        return claimRepository.getByDefendantId(id);
    }

    @Transactional
    public Claim saveClaim(String submitterId, ClaimData claimData, String authorisation) {
        String externalId = claimData.getExternalId().toString();

        caseRepository.getClaimByExternalId(externalId, authorisation).ifPresent(claim -> {
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

    public Claim requestMoreTimeForResponse(String externalId, String authorisation) {
        UserDetails defendant = userService.getUserDetails(authorisation);
        Claim claim = getClaimByExternalId(externalId, authorisation);

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

        claimRepository.requestMoreTime(claim.getId(), newDeadline);
        claim = getClaimByExternalId(externalId, authorisation);
        eventProducer.createMoreTimeForResponseRequestedEvent(claim, newDeadline, defendant.getEmail());

        return claim;
    }

    public Claim linkDefendantToClaim(String externalId, String defendantId, String authorisation) {
        return caseRepository.linkDefendant(externalId, defendantId, authorisation)
            .orElseThrow(() -> new NotFoundException("Claim not found by external id: " + externalId));
    }

    public void linkLetterHolder(Long claimId, String userId) {
        claimRepository.linkLetterHolder(claimId, userId);
    }

    public void linkSealedClaimDocument(Long claimId, String documentSelfPath) {
        claimRepository.linkSealedClaimDocument(claimId, documentSelfPath);
    }

    public void saveCountyCourtJudgment(String authorisation, Claim claim, CountyCourtJudgment countyCourtJudgment) {
        caseRepository.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment);
    }

    public void saveDefendantResponse(long claimId, String defendantId, String defendantEmail, Response response) {
        claimRepository.saveDefendantResponse(claimId, defendantId, defendantEmail, jsonMapper.toJson(response));
    }
}
