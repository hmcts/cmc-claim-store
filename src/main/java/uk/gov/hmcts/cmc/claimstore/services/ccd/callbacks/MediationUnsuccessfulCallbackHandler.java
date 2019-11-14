package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.claimstore.utils.DirectionsQuestionnaireUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.directionsquestionnaire.PilotCourt;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MEDIATION_UNSUCCESSFUL;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Service
public class MediationUnsuccessfulCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public MediationUnsuccessfulCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.SUBMITTED, this::notifyParties
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return ImmutableList.of(MEDIATION_UNSUCCESSFUL);
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private CallbackResponse notifyParties(CallbackParams callbackParams) {
        logger.info("Mediation unsuccessful callback: notifying parties");
        CallbackRequest callbackRequest = callbackParams.getRequest();
        Map<String, Object> caseData = callbackRequest.getCaseDetails().getData();
        Claim claim = caseDetailsConverter.extractClaim(CaseDetails.builder().data(caseData).build());
        if (DirectionsQuestionnaireUtils.isOnlineDQ(claim)) {
            notifyClaimant(claim);
            notifyDefendant(claim);
        } else {
            notifyClaimantOfflineJourney(claim);
            notifyDefendantOfflineJourney(claim);
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private void notifyClaimantOfflineJourney(Claim claim) {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantMediationFailureOfflineDQ(),
            aggregateParams(claim),
            NotificationReferenceBuilder.MediationUnsuccessful
                .forMediationUnsuccessfulOfflineDQ(claim.getReferenceNumber(), "claimant")
        );
    }

    private void notifyDefendantOfflineJourney(Claim claim) {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantMediationFailureOfflineDQ(),
            aggregateParams(claim),
            NotificationReferenceBuilder.MediationUnsuccessful
                .forMediationUnsuccessfulOfflineDQ(claim.getReferenceNumber(), "defendant")
        );
    }

    private void notifyClaimant(Claim claim) {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            isPilotCourt(claim)
                ? notificationsProperties.getTemplates().getEmail().getClaimantReadyForDirections() :
                notificationsProperties.getTemplates().getEmail().getClaimantReadyForTransfer(),
            aggregateParams(claim),
            isPilotCourt(claim)
                ? NotificationReferenceBuilder.MediationUnsuccessful
                .referenceForDirections(claim.getReferenceNumber(), "claimant") :
                NotificationReferenceBuilder.MediationUnsuccessful
                    .referenceForTransfer(claim.getReferenceNumber(), "claimant")
        );
    }

    private void notifyDefendant(Claim claim) {
        notificationService.sendMail(
            claim.getDefendantEmail(),
            isPilotCourt(claim)
                ? notificationsProperties.getTemplates().getEmail().getDefendantReadyForDirections() :
                notificationsProperties.getTemplates().getEmail().getDefendantReadyForTransfer(),
            aggregateParams(claim),
            isPilotCourt(claim)
                ? NotificationReferenceBuilder.MediationUnsuccessful
                .referenceForDirections(claim.getReferenceNumber(), "defendant") :
                NotificationReferenceBuilder.MediationUnsuccessful
                    .referenceForTransfer(claim.getReferenceNumber(), "defendant")
        );
    }

    private boolean isPilotCourt(Claim claim) {
        return PilotCourt.isPilotCourt(DirectionsQuestionnaireUtils.getPreferredCourt(claim));
    }

    private Map<String, String> aggregateParams(Claim claim) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        return parameters;
    }

}
