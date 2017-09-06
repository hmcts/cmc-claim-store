package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeRequestedAfterDeadlineException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.utils.LocalDateTimeFactory;

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

    public List<Claim> getClaimBySubmitterId(final long submitterId) {
        return claimRepository.getBySubmitterId(submitterId);
    }

    public Claim getClaimByLetterHolderId(final long id) {
        return claimRepository
            .getByLetterHolderId(id)
            .orElseThrow(() -> new NotFoundException("Claim not found for letter holder id " + id));
    }

    public Claim getClaimByExternalId(final String externalId) {
        return claimRepository
            .getClaimByExternalId(externalId)
            .orElseThrow(() -> new NotFoundException("Claim not found by external id " + externalId));
    }

    public List<Claim> getClaimByDefendantId(final long id) {
        return claimRepository.getByDefendantId(id);
    }

    @Transactional
    public Claim saveClaim(final long submitterId, final ClaimData claimData, final String authorisation) {
        final LocalDateTime now = LocalDateTimeFactory.nowInLocalZone();
        Optional<GeneratePinResponse> pinResponse = Optional.empty();

        if (!claimData.isClaimantRepresented()) {
            pinResponse = Optional.of(userService.generatePin(claimData.getDefendant().getName(), authorisation));
        }

        final Optional<Long> letterHolderId = pinResponse.map(GeneratePinResponse::getUserId);
        final LocalDate issuedOn = issueDateCalculator.calculateIssueDay(now);
        final LocalDate responseDeadline = responseDeadlineCalculator.calculateResponseDeadline(issuedOn);
        final String submitterEmail = userService.getUserDetails(authorisation).getEmail();

        final String claimDataString = jsonMapper.toJson(claimData);
        final String externalId = claimData.getExternalId().toString();

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
            pinResponse.map(GeneratePinResponse::getPin)
                .orElse(null));

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
            .calculatePostponedResponseDeadline(claim.getResponseDeadline());

        claimRepository.requestMoreTime(claimId, newDeadline);
        claim = getClaimById(claimId);
        eventProducer.createMoreTimeForResponseRequestedEvent(claim, newDeadline, defendant.getEmail());

        return claim;
    }

    public void linkDefendantToClaim(Long claimId, Long defendantId) {
        final Claim claim = claimRepository.getById(claimId)
            .orElseThrow(() -> new NotFoundException("Claim not found by id: " + claimId));

        claimRepository.linkDefendant(claim.getId(), defendantId);
    }
}
