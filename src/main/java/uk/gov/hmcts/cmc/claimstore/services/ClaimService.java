package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
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
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.claimstore.utils.CCDCaseDataToClaim;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.response.CaseReference;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_ISSUED_CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_ISSUED_LEGAL;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_MORE_TIME_REQUESTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_MORE_TIME_REQUESTED_PAPER;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@Component
public class ClaimService {
    private final Logger logger = LoggerFactory.getLogger(ClaimService.class);

    private final ClaimRepository claimRepository;
    private final IssueDateCalculator issueDateCalculator;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final LegalOrderGenerationDeadlinesCalculator
        legalOrderGenerationDeadlinesCalculator;
    private final DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator;
    private final UserService userService;
    private final EventProducer eventProducer;
    private final CaseRepository caseRepository;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final AppInsights appInsights;
    private final CCDCaseDataToClaim ccdCaseDataToClaim;
    private final PaidInFullRule paidInFullRule;
    private final ClaimAuthorisationRule claimAuthorisationRule;
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
        LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator,
        DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator,
        MoreTimeRequestRule moreTimeRequestRule,
        EventProducer eventProducer,
        AppInsights appInsights,
        CCDCaseDataToClaim ccdCaseDataToClaim,
        PaidInFullRule paidInFullRule,
        CCDEventProducer ccdEventProducer,
        ClaimAuthorisationRule claimAuthorisationRule,
        @Value("feature_toggles.async_event_operations_enabled") String asyncEventOperationEnabled
    ) {
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.issueDateCalculator = issueDateCalculator;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.legalOrderGenerationDeadlinesCalculator = legalOrderGenerationDeadlinesCalculator;
        this.eventProducer = eventProducer;
        this.caseRepository = caseRepository;
        this.moreTimeRequestRule = moreTimeRequestRule;
        this.appInsights = appInsights;
        this.ccdCaseDataToClaim = ccdCaseDataToClaim;
        this.directionsQuestionnaireDeadlineCalculator = directionsQuestionnaireDeadlineCalculator;
        this.paidInFullRule = paidInFullRule;
        this.ccdEventProducer = ccdEventProducer;
        this.claimAuthorisationRule = claimAuthorisationRule;
        this.asyncEventOperationEnabled = Boolean.getBoolean(asyncEventOperationEnabled);
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

    public CaseReference savePrePayment(String externalId, String authorisation) {
        return caseRepository.savePrePaymentClaim(externalId, authorisation);
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
            .responseDeadline(responseDeadline)
            .externalId(externalId)
            .submitterEmail(submitterEmail)
            .createdAt(nowInUTC())
            .letterHolderId(letterHolderId.orElse(null))
            .features(features)
            .build();

        Claim savedClaim = caseRepository.saveClaim(user, claim);
        ccdEventProducer.createCCDClaimIssuedEvent(savedClaim, user);

        eventProducer.createClaimIssuedEvent(
            savedClaim,
            pinResponse.map(GeneratePinResponse::getPin).orElse(null),
            user.getUserDetails().getFullName(),
            authorisation
        );

        trackClaimIssued(savedClaim.getReferenceNumber(), savedClaim.getClaimData().isClaimantRepresented());

        return savedClaim;

    }

    public Optional<GeneratePinResponse> getPinResponse(ClaimData claimData, String authorisation) {
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

    public AboutToStartOrSubmitCallbackResponse prepopulateFields(CallbackRequest callbackRequest) {
        logger.info("Prepopulating fields for callback {}", callbackRequest.getEventId());
        LocalDate deadline = legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines();
        Map<String, Object> data = new HashMap<>();
        data.put("docUploadDeadline", deadline);
        data.put("eyewitnessUploadDeadline", deadline);
        data.put("directionList", ImmutableList.of(
            CCDOrderDirectionType.DOCUMENTS.name(),
            CCDOrderDirectionType.EYEWITNESS.name()));
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(data)
            .build();
    }

    public AboutToStartOrSubmitCallbackResponse requestMoreTimeOnPaperValidateOnly(
        CallbackRequest callbackRequest
    ) {
        return requestMoreTimeOnPaper(callbackRequest, true);
    }

    public AboutToStartOrSubmitCallbackResponse requestMoreTimeOnPaper(
        CallbackRequest callbackRequest
    ) {
        return requestMoreTimeOnPaper(callbackRequest, false);
    }

    private AboutToStartOrSubmitCallbackResponse requestMoreTimeOnPaper(
        CallbackRequest callbackRequest,
        boolean validateOnly
    ) {
        Claim claim = convertCallbackToClaim(callbackRequest);

        List<String> validationResult = this.moreTimeRequestRule.validateMoreTimeCanBeRequested(claim);
        AboutToStartOrSubmitCallbackResponseBuilder builder = AboutToStartOrSubmitCallbackResponse
            .builder();

        if (validateOnly || !validationResult.isEmpty()) {
            return builder
                .errors(validationResult)
                .build();
        }

        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());

        Map<String, Object> data = new HashMap<>(callbackRequest.getCaseDetails().getData());
        data.put("moreTimeRequested", CCDYesNoOption.YES);
        data.put("responseDeadline", newDeadline);

        return builder
            .data(data)
            .build();
    }

    public SubmittedCallbackResponse requestMoreTimeOnPaperSubmitted(CallbackRequest callbackRequest) {
        Claim claim = convertCallbackToClaim(callbackRequest);

        eventProducer.createMoreTimeForResponseRequestedEvent(
            claim,
            claim.getResponseDeadline(),
            claim.getClaimData().getDefendant().getEmail().orElse(null)
        );
        appInsights.trackEvent(RESPONSE_MORE_TIME_REQUESTED_PAPER, REFERENCE_NUMBER, claim.getReferenceNumber());

        return SubmittedCallbackResponse.builder()
            .build();
    }

    private Claim convertCallbackToClaim(CallbackRequest caseDetails) {
        return ccdCaseDataToClaim.to(
            caseDetails.getCaseDetails().getId(),
            caseDetails.getCaseDetails().getData()
        );
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

    public Claim linkLetterHolder(Long claimId, String letterHolderId) {
        return caseRepository.linkLetterHolder(claimId, letterHolderId);
    }

    public void saveCountyCourtJudgment(
        String authorisation,
        Claim claim,
        CountyCourtJudgment countyCourtJudgment
    ) {
        claimAuthorisationRule.assertClaimCanBeAccessed(claim, authorisation);
        caseRepository.saveCountyCourtJudgment(authorisation, claim, countyCourtJudgment);
        ccdEventProducer.createCCDCountyCourtJudgmentEvent(claim, authorisation, countyCourtJudgment);
        appInsights.trackEvent(CCJ_REQUESTED, REFERENCE_NUMBER, claim.getReferenceNumber());
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

    public void updateClaimState(String authorisation, Claim claim, ClaimState state){
        claimRepository.updateClaimState(authorisation, claim.getId(), state.name());
    }
}
