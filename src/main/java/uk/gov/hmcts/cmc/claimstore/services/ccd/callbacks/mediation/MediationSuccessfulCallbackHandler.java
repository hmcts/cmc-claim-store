package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.mediation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MediationSuccessful;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.MEDIATION_PILOT_SUCCESS;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NON_MEDIATION_PILOT_SUCCESS;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Service
public class MediationSuccessfulCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> MEDIATION_SUCCESSFUL = ImmutableList.of(CaseEvent.MEDIATION_SUCCESSFUL);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final AppInsights appInsights;

    @Autowired
    public MediationSuccessfulCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        AppInsights appInsights
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.appInsights = appInsights;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.SUBMITTED, this::notifyParties
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return MEDIATION_SUCCESSFUL;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    public CallbackResponse notifyParties(CallbackParams callbackParams) {
        logger.info("Mediation successful callback: notifying parties");
        CallbackRequest callbackRequest = callbackParams.getRequest();
        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        notifyClaimant(claim);
        notifyDefendant(claim);
        raiseAppInsightEvent(claim);
        return SubmittedCallbackResponse.builder().build();
    }

    private void notifyClaimant(Claim claim) {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantMediationSuccess(),
            aggregateParams(claim),
            MediationSuccessful
                .referenceForClaimant(claim.getReferenceNumber(), "claimant")
        );
    }

    private void notifyDefendant(Claim claim) {
        notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getDefendantMediationSuccess(),
            aggregateParams(claim),
            MediationSuccessful.referenceForDefendant(claim.getReferenceNumber(), "defendant")
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        return parameters;
    }

    private void raiseAppInsightEvent(Claim claim) {
        appInsights
            .trackEvent(getAppInsightEventBasedOnMediationPilot(claim), REFERENCE_NUMBER, claim.getReferenceNumber());
    }

    private AppInsightsEvent getAppInsightEventBasedOnMediationPilot(Claim claim) {
        return FeaturesUtils.hasMediationPilotFeature(claim)
            ? MEDIATION_PILOT_SUCCESS
            : NON_MEDIATION_PILOT_SUCCESS;
    }
}
