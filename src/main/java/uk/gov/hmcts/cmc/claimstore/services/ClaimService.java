package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_ISSUED_CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_ISSUED_LEGAL;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_MORE_TIME_REQUESTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_SUBMITTED;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@Component
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final IssueDateCalculator issueDateCalculator;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final UserService userService;
    private final EventProducer eventProducer;
    private final CaseRepository caseRepository;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final AppInsights appInsights;

    @SuppressWarnings("squid:S00107") //Constructor need all parameters
    @Autowired
    public ClaimService(
        ClaimRepository claimRepository,
        UserService userService,
        IssueDateCalculator issueDateCalculator,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        EventProducer eventProducer,
        CaseRepository caseRepository,
        MoreTimeRequestRule moreTimeRequestRule,
        AppInsights appInsights
    ) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.issueDateCalculator = issueDateCalculator;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.eventProducer = eventProducer;
        this.caseRepository = caseRepository;
        this.moreTimeRequestRule = moreTimeRequestRule;
        this.appInsights = appInsights;
    }

    public Claim getClaimById(long claimId) {
        return claimRepository
            .getById(claimId)
            .orElseThrow(() -> new NotFoundException("Claim not found by id " + claimId));
    }

    public List<Claim> getClaimBySubmitterId(String submitterId, String authorisation) {
        return caseRepository.getBySubmitterId(submitterId, authorisation);
    }

    public Claim getClaimByLetterHolderId(String id, String authorisation) {
        return caseRepository
            .getByLetterHolderId(id, authorisation)
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

    public List<Claim> getClaimByDefendantId(String id, String authorisation) {
        return caseRepository.getByDefendantId(id, authorisation);
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

        final Claim claim = Claim.builder()
            .claimData(claimData)
            .submitterId(submitterId)
            .issuedOn(issuedOn)
            .responseDeadline(responseDeadline)
            .externalId(externalId)
            .submitterEmail(submitterEmail)
            .createdAt(nowInUTC())
            .letterHolderId(letterHolderId.orElse(null))
            .build();

        Claim issuedClaim = caseRepository.saveClaim(authorisation, claim);

        eventProducer.createClaimIssuedEvent(
            issuedClaim,
            pinResponse.map(GeneratePinResponse::getPin).orElse(null),
            userDetails.getFullName(),
            authorisation
        );

        Claim retrievedClaim = getClaimByExternalId(externalId, authorisation);
        trackClaimIssued(retrievedClaim.getReferenceNumber(), claim.getClaimData().isClaimantRepresented());
        return retrievedClaim;
    }

    private void trackClaimIssued(String referenceNumber, boolean represented) {
        if (represented) {
            appInsights.trackEvent(CLAIM_ISSUED_LEGAL, referenceNumber);
        } else {
            appInsights.trackEvent(CLAIM_ISSUED_CITIZEN, referenceNumber);
        }
    }

    public Claim requestMoreTimeForResponse(String externalId, String authorisation) {
        Claim claim = getClaimByExternalId(externalId, authorisation);

        this.moreTimeRequestRule.assertMoreTimeCanBeRequested(claim);

        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());

        caseRepository.requestMoreTimeForResponse(authorisation, claim, newDeadline);

        claim = getClaimByExternalId(externalId, authorisation);
        UserDetails defendant = userService.getUserDetails(authorisation);
        eventProducer.createMoreTimeForResponseRequestedEvent(claim, newDeadline, defendant.getEmail());

        appInsights.trackEvent(RESPONSE_MORE_TIME_REQUESTED, claim.getReferenceNumber());
        return claim;
    }

    public void linkDefendantToClaim(String authorisation) {
        caseRepository.linkDefendant(authorisation);
    }

    public void linkLetterHolder(Long claimId, String userId) {
        claimRepository.linkLetterHolder(claimId, userId);
    }

    public void linkSealedClaimDocument(Long claimId, String documentSelfPath) {
        claimRepository.linkSealedClaimDocument(claimId, documentSelfPath);
    }

    public void saveCountyCourtJudgment(String authorisation, Claim claim, CountyCourtJudgment countyCourtJudgment) {
        caseRepository.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment);
        appInsights.trackEvent(CCJ_REQUESTED, claim.getReferenceNumber());
    }

    public void saveDefendantResponse(
        Claim claim,
        String defendantEmail,
        Response response,
        String authorization
    ) {
        caseRepository.saveDefendantResponse(claim, defendantEmail, response, authorization);
        appInsights.trackEvent(RESPONSE_SUBMITTED, claim.getReferenceNumber());
    }
}
