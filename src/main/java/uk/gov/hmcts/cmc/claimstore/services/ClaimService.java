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
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ResponseData;
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

    @Autowired
    public ClaimService(
        final ClaimRepository claimRepository,
        final UserService userService,
        final JsonMapper jsonMapper,
        final IssueDateCalculator issueDateCalculator,
        final ResponseDeadlineCalculator responseDeadlineCalculator,
        final EventProducer eventProducer) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.jsonMapper = jsonMapper;
        this.issueDateCalculator = issueDateCalculator;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.eventProducer = eventProducer;
    }

    public Claim getClaimById(final long claimId) {
        return claimRepository
            .getById(claimId)
            .orElseThrow(() -> new NotFoundException("Claim not found by id " + claimId));
    }

    public List<Claim> getClaimBySubmitterId(final String submitterId) {
        return claimRepository.getBySubmitterId(submitterId);
    }

    public Claim getClaimByLetterHolderId(final String id) {
        return claimRepository
            .getByLetterHolderId(id)
            .orElseThrow(() -> new NotFoundException("Claim not found for letter holder id " + id));
    }

    public Claim getClaimByExternalId(final String externalId) {
        return claimRepository
            .getClaimByExternalId(externalId)
            .orElseThrow(() -> new NotFoundException("Claim not found by external id " + externalId));
    }

    public Optional<Claim> getClaimByReference(final String reference, final String authorisation) {
        final String submitterId = userService.getUserDetails(authorisation).getId();
        return claimRepository
            .getByClaimReferenceAndSubmitter(reference, submitterId);
    }

    public Optional<Claim> getClaimByReference(final String reference) {
        return claimRepository
            .getByClaimReferenceNumber(reference);
    }

    public List<Claim> getClaimByExternalReference(final String externalReference, final String authorisation) {
        final String submitterId = userService.getUserDetails(authorisation).getId();
        return claimRepository.getByExternalReference(externalReference, submitterId);
    }

    public List<Claim> getClaimByDefendantId(final String id) {
        return claimRepository.getByDefendantId(id);
    }

    @Transactional
    public Claim saveClaim(final String submitterId, final ClaimData claimData, final String authorisation) {
        final String externalId = claimData.getExternalId().toString();

        claimRepository.getClaimByExternalId(externalId).ifPresent(claim -> {
            throw new ConflictException("Duplicate claim for external id " + claim.getExternalId());
        });

        final LocalDateTime now = LocalDateTimeFactory.nowInLocalZone();
        Optional<GeneratePinResponse> pinResponse = Optional.empty();

        if (!claimData.isClaimantRepresented()) {
            pinResponse = Optional.of(userService.generatePin(claimData.getDefendant().getName(), authorisation));
        }

        final Optional<String> letterHolderId = pinResponse.map(GeneratePinResponse::getUserId);
        final LocalDate issuedOn = issueDateCalculator.calculateIssueDay(now);
        final LocalDate responseDeadline = responseDeadlineCalculator.calculateResponseDeadline(issuedOn);
        final UserDetails userDetails = userService.getUserDetails(authorisation);
        final String submitterEmail = userDetails.getEmail();

        final String claimDataString = jsonMapper.toJson(claimData);

        long issuedClaimId;

        if (claimData.isClaimantRepresented()) {
            issuedClaimId = claimRepository.saveRepresented(claimDataString, submitterId, issuedOn,
                responseDeadline, externalId, submitterEmail);
        } else {
            issuedClaimId = claimRepository.saveSubmittedByClaimant(claimDataString, submitterId,
                letterHolderId.orElseThrow(IllegalStateException::new), issuedOn, responseDeadline,
                externalId, submitterEmail);
        }

        final Claim claim = getClaimById(issuedClaimId);

        eventProducer.createClaimIssuedEvent(claim,
            pinResponse.map(GeneratePinResponse::getPin).orElse(null),
            userDetails.getFullName(), authorisation);

        return claim;
    }

    public Claim requestMoreTimeForResponse(final long claimId, final String authorisation) {
        final UserDetails defendant = userService.getUserDetails(authorisation);
        Claim claim = getClaimById(claimId);

        if (!claim.getDefendantId()
            .equals(defendant.getId())) {
            throw new ForbiddenActionException("This claim is not raised against you");
        }

        if (claim.isMoreTimeRequested()) {
            throw new MoreTimeAlreadyRequestedException("You have already requested more time");
        }

        if (LocalDate.now()
            .isAfter(claim.getResponseDeadline())) {
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
        final Claim claim = claimRepository.getById(claimId)
            .orElseThrow(() -> new NotFoundException("Claim not found by id: " + claimId));

        claimRepository.linkDefendant(claim.getId(), defendantId);
    }

    public void linkLetterHolder(final Long claimId, final String userId) {
        claimRepository.linkLetterHolder(claimId, userId);
    }

    void saveCountyCourtJudgment(final long claimId, final CountyCourtJudgment countyCourtJudgment) {
        claimRepository.saveCountyCourtJudgment(claimId, jsonMapper.toJson(countyCourtJudgment));
    }

    void saveDefendantResponse(final long claimId, final String defendantId, final String defendantEmail,
                               final ResponseData responseData) {
        claimRepository.saveDefendantResponse(claimId, defendantId, defendantEmail, jsonMapper.toJson(responseData));
    }
}
