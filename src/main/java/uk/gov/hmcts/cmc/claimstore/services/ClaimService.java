package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.CCDEventProducer;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimAuthorisationRule;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.rules.PaidInFullRule;
import uk.gov.hmcts.cmc.claimstore.rules.ReviewOrderRule;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_ISSUED_CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_ISSUED_LEGAL;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_MORE_TIME_REQUESTED;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.OPEN;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Component
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final IssueDateCalculator issueDateCalculator;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator;
    private final UserService userService;
    private final EventProducer eventProducer;
    private final CaseRepository caseRepository;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final AppInsights appInsights;
    private final PaidInFullRule paidInFullRule;
    private final ClaimAuthorisationRule claimAuthorisationRule;
    private final ReviewOrderRule reviewOrderRule;
    private final boolean asyncEventOperationEnabled;
    private CCDEventProducer ccdEventProducer;

    @SuppressWarnings("squid:S00107") //Constructor need all parameters
    @Autowired
    public ClaimService(
        ClaimRepository claimRepository,
        CaseRepository caseRepository,
        UserService userService,
        IssueDateCalculator issueDateCalculator,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator,
        MoreTimeRequestRule moreTimeRequestRule,
        EventProducer eventProducer,
        AppInsights appInsights,
        PaidInFullRule paidInFullRule,
        CCDEventProducer ccdEventProducer,
        ClaimAuthorisationRule claimAuthorisationRule,
        ReviewOrderRule reviewOrderRule,
        @Value("${feature_toggles.async_event_operations_enabled:false}") boolean asyncEventOperationEnabled
    ) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.issueDateCalculator = issueDateCalculator;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.eventProducer = eventProducer;
        this.caseRepository = caseRepository;
        this.moreTimeRequestRule = moreTimeRequestRule;
        this.appInsights = appInsights;
        this.directionsQuestionnaireDeadlineCalculator = directionsQuestionnaireDeadlineCalculator;
        this.paidInFullRule = paidInFullRule;
        this.ccdEventProducer = ccdEventProducer;
        this.claimAuthorisationRule = claimAuthorisationRule;
        this.reviewOrderRule = reviewOrderRule;
        this.asyncEventOperationEnabled = asyncEventOperationEnabled;
    }

    public Claim getClaimById(long claimId) {
        return claimRepository
            .getById(claimId)
            .orElseThrow(() -> new NotFoundException("Claim not found by id " + claimId));
    }

    public List<Claim> getClaimBySubmitterId(String submitterId, String authorisation) {
        claimAuthorisationRule.assertSubmitterIdMatchesAuthorisation(submitterId, authorisation);
        return caseRepository.getBySubmitterId(submitterId, authorisation);
    }

    public Claim getClaimByLetterHolderId(String id, String authorisation) {
        Claim claim = caseRepository
            .getByLetterHolderId(id, authorisation)
            .orElseThrow(() -> new NotFoundException("Claim not found for letter holder id " + id));

        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorisation);

        return claim;
    }

    public Claim getClaimByExternalId(String externalId, String authorisation) {
        User user = userService.getUser(authorisation);
        return getClaimByExternalId(externalId, user);
    }

    public Claim getClaimByExternalId(String externalId, User user) {
        Claim claim = caseRepository
            .getClaimByExternalId(externalId, user)
            .orElseThrow(() -> new NotFoundException("Claim not found by external id " + externalId));

        claimAuthorisationRule.assertClaimCanBeAccessed(claim, user);

        return claim;
    }

    public Optional<Claim> getClaimByReference(String reference, String authorisation) {
        Optional<Claim> claim = caseRepository
            .getByClaimReferenceNumber(reference, authorisation);

        claim.ifPresent(c -> claimAuthorisationRule.assertClaimCanBeAccessed(c, authorisation));

        return claim;
    }

    public Optional<Claim> getClaimByReferenceAnonymous(String reference) {
        String authorisation = null;

        if (caseRepository instanceof CCDCaseRepository) {
            User user = userService.authenticateAnonymousCaseWorker();
            authorisation = user.getAuthorisation();
        }

        return caseRepository.getByClaimReferenceNumber(reference, authorisation);
    }

    public List<Claim> getClaimByExternalReference(String externalReference, String authorisation) {
        String submitterId = userService.getUserDetails(authorisation).getId();

        return claimRepository.getByExternalReference(externalReference, submitterId);
    }

    public List<Claim> getClaimByDefendantId(String id, String authorisation) {
        claimAuthorisationRule.assertSubmitterIdMatchesAuthorisation(id, authorisation);

        return caseRepository.getByDefendantId(id, authorisation);
    }

    public List<Claim> getClaimByClaimantEmail(String email, String authorisation) {
        return caseRepository.getByClaimantEmail(email, authorisation);
    }

    public List<Claim> getClaimByDefendantEmail(String email, String authorisation) {
        return caseRepository.getByDefendantEmail(email, authorisation);
    }

    public List<Claim> getClaimByPaymentReference(String payReference, String authorisation) {
        return caseRepository.getByPaymentReference(payReference, authorisation);
    }

    public List<Claim> getClaimsByState(ClaimState claimState, User user) {
        return caseRepository.getClaimsByState(claimState, user);
    }

    @LogExecutionTime
    @Transactional(transactionManager = "transactionManager")
    public Claim saveClaim(
        String submitterId,
        ClaimData claimData,
        String authorisation,
        List<String> features
    ) {
        String externalId = claimData.getExternalId().toString();
        User user = userService.getUser(authorisation);

        caseRepository.getClaimByExternalId(externalId, user).ifPresent(claim -> {
            throw new ConflictException(
                String.format("Claim already exist with same external reference as %s", externalId));
        });

        Optional<GeneratePinResponse> pinResponse = getPinResponse(claimData, authorisation);
        Optional<String> letterHolderId = pinResponse.map(GeneratePinResponse::getUserId);
        LocalDate issuedOn = issueDateCalculator.calculateIssueDay(nowInLocalZone());
        LocalDate responseDeadline = responseDeadlineCalculator.calculateResponseDeadline(issuedOn);
        String submitterEmail = user.getUserDetails().getEmail();

        Claim claim = Claim.builder()
            .claimData(claimData)
            .submitterId(submitterId)
            .issuedOn(issuedOn)
            .serviceDate(issuedOn.plusDays(5))
            .responseDeadline(responseDeadline)
            .externalId(externalId)
            .submitterEmail(submitterEmail)
            .createdAt(nowInLocalZone())
            .letterHolderId(letterHolderId.orElse(null))
            .features(features)
            .claimSubmissionOperationIndicators(ClaimSubmissionOperationIndicators.builder().build())
            .build();

        Claim savedClaim = caseRepository.saveClaim(user, claim);
        ccdEventProducer.createCCDClaimIssuedEvent(savedClaim, user);

        if (asyncEventOperationEnabled) {
            eventProducer.createClaimCreatedEvent(
                savedClaim,
                pinResponse.map(GeneratePinResponse::getPin).orElse(null),
                user.getUserDetails().getFullName(),
                authorisation
            );
        } else {
            eventProducer.createClaimIssuedEvent(
                savedClaim,
                pinResponse.map(GeneratePinResponse::getPin).orElse(null),
                user.getUserDetails().getFullName(),
                authorisation
            );
            caseRepository.updateClaimState(user.getAuthorisation(), savedClaim.getId(), OPEN);
        }
        trackClaimIssued(savedClaim.getReferenceNumber(), savedClaim.getClaimData().isClaimantRepresented());

        return savedClaim;
    }

    private Optional<GeneratePinResponse> getPinResponse(ClaimData claimData, String authorisation) {
        if (!claimData.isClaimantRepresented() && !asyncEventOperationEnabled) {
            return Optional.of(userService.generatePin(claimData.getDefendant().getName(), authorisation));
        }

        return Optional.empty();
    }

    private void trackClaimIssued(String referenceNumber, boolean represented) {
        AppInsightsEvent event = represented ? CLAIM_ISSUED_LEGAL : CLAIM_ISSUED_CITIZEN;
        appInsights.trackEvent(event, REFERENCE_NUMBER, referenceNumber);
    }

    public Claim requestMoreTimeForResponse(String externalId, String authorisation) {
        Claim claim = getClaimByExternalId(externalId, authorisation);

        this.moreTimeRequestRule.assertMoreTimeCanBeRequested(claim);

        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());

        caseRepository.requestMoreTimeForResponse(authorisation, claim, newDeadline);

        claim = getClaimByExternalId(externalId, authorisation);
        UserDetails defendant = userService.getUserDetails(authorisation);
        eventProducer.createMoreTimeForResponseRequestedEvent(claim, newDeadline, defendant.getEmail());
        ccdEventProducer.createMoreTimeForCCDResponseRequestedEvent(authorisation, externalId, newDeadline);

        appInsights.trackEvent(RESPONSE_MORE_TIME_REQUESTED, REFERENCE_NUMBER, claim.getReferenceNumber());
        return claim;
    }

    public void linkDefendantToClaim(String authorisation) {
        caseRepository.linkDefendant(authorisation);
        ccdEventProducer.linkDefendantCCDEvent(authorisation);
    }

    public Claim saveClaimDocuments(
        String authorisation,
        Long claimId,
        ClaimDocumentCollection claimDocumentCollection,
        ClaimDocumentType claimDocumentType
    ) {
        return caseRepository.saveClaimDocuments(authorisation, claimId, claimDocumentCollection, claimDocumentType);
    }

    public Claim linkLetterHolder(Claim claim, String letterHolderId, String authorisation) {
        Claim updated = caseRepository.linkLetterHolder(claim.getId(), letterHolderId);
        ccdEventProducer.createCCDLinkLetterHolderEvent(claim, letterHolderId, authorisation);
        return updated;
    }

    public void saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment
    ) {
        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorisation);
        caseRepository.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment);
        ccdEventProducer.createCCDCountyCourtJudgmentEvent(claim, authorisation, countyCourtJudgment);
    }

    public void saveDefendantResponse(
        Claim claim,
        String defendantEmail,
        Response response,
        String authorization
    ) {
        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorization);
        LocalDate claimantResponseDeadline =
            responseDeadlineCalculator.calculateClaimantResponseDeadline(LocalDate.now());
        caseRepository.saveDefendantResponse(claim, defendantEmail, response, claimantResponseDeadline, authorization);
        if (isFullDefenceWithNoMediation(response)) {
            LocalDate deadline = directionsQuestionnaireDeadlineCalculator
                .calculateDirectionsQuestionnaireDeadlineCalculator(LocalDateTime.now());
            caseRepository.updateDirectionsQuestionnaireDeadline(claim, deadline, authorization);
        }
    }

    public Claim paidInFull(String externalId, PaidInFull paidInFull, String authorisation) {
        Claim claim = getClaimByExternalId(externalId, authorisation);
        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorisation);
        String claimantId = userService.getUserDetails(authorisation).getId();
        this.paidInFullRule.assertPaidInFull(claim, claimantId);
        this.caseRepository.paidInFull(claim, paidInFull, authorisation);
        Claim updatedClaim = getClaimByExternalId(externalId, authorisation);
        this.eventProducer.createPaidInFullEvent(updatedClaim);
        appInsights.trackEvent(AppInsightsEvent.PAID_IN_FULL, REFERENCE_NUMBER, claim.getReferenceNumber());
        this.ccdEventProducer.createCCDPaidInFullEvent(authorisation, claim, paidInFull);
        return updatedClaim;
    }

    private static boolean isFullDefenceWithNoMediation(Response response) {
        return response.getResponseType().equals(ResponseType.FULL_DEFENCE)
            && response.getFreeMediation().filter(Predicate.isEqual(YesNoOption.NO)).isPresent();
    }

    public void saveReDetermination(
        String authorisation,
        Claim claim,
        ReDetermination redetermination
    ) {
        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorisation);
        caseRepository.saveReDetermination(authorisation, claim, redetermination);
        ccdEventProducer.createCCDReDetermination(claim, authorisation, redetermination);
    }

    public void updateClaimState(String authorisation, Claim claim, ClaimState state) {
        caseRepository.updateClaimState(authorisation, claim.getId(), state);
    }

    public Claim saveReviewOrder(String externalId, ReviewOrder reviewOrder, String authorisation) {
        Claim claim = getClaimByExternalId(externalId, authorisation);
        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorisation);
        reviewOrderRule.assertReviewOrder(claim);
        Claim updatedClaim = caseRepository.saveReviewOrder(claim.getId(), reviewOrder, authorisation);
        eventProducer.createReviewOrderEvent(authorisation, updatedClaim);
        return updatedClaim;
    }
}
