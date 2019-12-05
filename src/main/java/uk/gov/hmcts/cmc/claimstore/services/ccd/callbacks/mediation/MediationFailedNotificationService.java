package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.mediation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.DirectionsQuestionnaireUtils;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DQ_DEADLINE;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Service
public class MediationFailedNotificationService {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public MediationFailedNotificationService(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    protected void notifyParties(Claim claim) {
        if (DirectionsQuestionnaireUtils.isOnlineDQ(claim)) {
            notifyClaimant(claim);
            notifyDefendant(claim);
        } else {
            notifyClaimantOfflineJourney(claim);
            notifyDefendantOfflineJourney(claim);
        }
    }

    private void notifyClaimant(Claim claim) {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantReadyForTransfer(),
            aggregateParams(claim),
            NotificationReferenceBuilder.MediationUnsuccessful
                .referenceForTransfer(claim.getReferenceNumber(), "claimant")
        );
    }

    private void notifyDefendant(Claim claim) {
        notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getDefendantReadyForTransfer(),
            aggregateParams(claim),
            NotificationReferenceBuilder.MediationUnsuccessful
                .referenceForTransfer(claim.getReferenceNumber(), "defendant")
        );
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
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getDefendantMediationFailureOfflineDQ(),
            aggregateParams(claim),
            NotificationReferenceBuilder.MediationUnsuccessful
                .forMediationUnsuccessfulOfflineDQ(claim.getReferenceNumber(), "defendant")
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        if (!DirectionsQuestionnaireUtils.isOnlineDQ(claim)) {
            parameters.put(DQ_DEADLINE, Formatting.formatDate(claim.getDirectionsQuestionnaireDeadline()));
        }
        return parameters;
    }
}
