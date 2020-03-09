package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.mediation;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.MEDIATION_PILOT_FAILED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NON_MEDIATION_PILOT_FAILED;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.utils.ResponseHelper.isResponsePartOrFullDefence;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType.REJECTION;

@Service
public class MediationFailedCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(CaseEvent.MEDIATION_FAILED);

    private static final String STATE = "state";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CaseDetailsConverter caseDetailsConverter;

    private final DirectionsQuestionnaireDeadlineCalculator deadlineCalculator;

    private final CaseMapper caseMapper;
    private final AppInsights appInsights;
    private final DirectionsQuestionnaireService directionsQuestionnaireService;
    private final CoreCaseDataService coreCaseDataService;


    private final MediationFailedNotificationService notificationService;

    @Autowired
    public MediationFailedCallbackHandler(CaseDetailsConverter caseDetailsConverter,
                                          DirectionsQuestionnaireDeadlineCalculator deadlineCalculator,
                                          CaseMapper caseMapper,
                                          AppInsights appInsights,
                                          MediationFailedNotificationService notificationService,
                                          DirectionsQuestionnaireService directionsQuestionnaireService,
                                          CoreCaseDataService coreCaseDataService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.deadlineCalculator = deadlineCalculator;
        this.caseMapper = caseMapper;
        this.notificationService = notificationService;
        this.appInsights = appInsights;
        this.directionsQuestionnaireService = directionsQuestionnaireService;
        this.coreCaseDataService = coreCaseDataService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
                CallbackType.ABOUT_TO_SUBMIT, this::assignCaseState,
                CallbackType.SUBMITTED, this::notifyPartiesOfOutcome
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private CallbackResponse notifyPartiesOfOutcome(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();

        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        appInsights
                .trackEvent(getAppInsightEventBasedOnMediationPilot(claim), REFERENCE_NUMBER, claim.getReferenceNumber());

        notificationService.notifyParties(claim);

        if (!FeaturesUtils.isOnlineDQ(claim)) {
            offlineDQCheck(callbackParams, claim);
        }
        return SubmittedCallbackResponse.builder().build();
    }

    private CallbackResponse assignCaseState(CallbackParams callbackParams) {
        logger.info("Mediation failure about-to-submit callback: state determination start");
        CallbackRequest callbackRequest = callbackParams.getRequest();

        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());

        if (!FeaturesUtils.isOnlineDQ(claim)) {
            LocalDate deadline = deadlineCalculator.calculateDirectionsQuestionnaireDeadline(LocalDateTime.now());
            claim = claim.toBuilder().directionsQuestionnaireDeadline(deadline).build();
        }

        Map<String, Object> dataMap = caseDetailsConverter.convertToMap(caseMapper.to(claim));
        dataMap.put(STATE, stateByOnlineDQnPilotCheck(claim));

        logger.info("Mediation failure about-to-submit callback: state determined - {}", dataMap.get(STATE));

        return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(dataMap)
                .build();
    }

    private void offlineDQCheck(CallbackParams callbackParams, Claim claim) {
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        coreCaseDataService.saveCaseEvent(authorisation, claim.getCcdCaseId(), CaseEvent.DIRECTIONS_QUESTIONNAIRE_DEADLINE);
    }

    private String stateByOnlineDQnPilotCheck(Claim claim) {

        if (!claim.getResponse().filter(isResponsePartOrFullDefence).isPresent()) {
            throw new IllegalStateException("There was no Mediation involved as its not full defence or part admit");
        }

        if (!claim.getClaimantResponse()
                .map(ClaimantResponse::getType)
                .filter(REJECTION::equals).isPresent()) {
            throw new IllegalStateException("Claimant response is not an Rejection");
        }

        if (!FeaturesUtils.isOnlineDQ(claim)) {
            return ClaimState.OPEN.getValue();
        }

        return directionsQuestionnaireService.getDirectionsCaseState(claim);
    }

    private AppInsightsEvent getAppInsightEventBasedOnMediationPilot(Claim claim) {
        return FeaturesUtils.hasMediationPilotFeature(claim)
                ? MEDIATION_PILOT_FAILED
                : NON_MEDIATION_PILOT_FAILED;
    }

}
