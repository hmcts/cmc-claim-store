package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

public class ResendPinHandler {

    private NotificationService notificationService;
    private NotificationsProperties notificationsProperties;
    private GeneratedDocuments generatedDocuments;

    @Autowired
    public ResendPinHandler(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        GeneratedDocuments generatedDocuments
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.generatedDocuments = generatedDocuments;
    }

    @EventListener
    public void sendDefendantNotification(ResendPinEvent resendPinEvent) {
        Claim claim = resendPinEvent.getClaim();

        notificationService.sendMail(
            claim.getDefendantEmail(),
            //is this right template?
            notificationsProperties.getTemplates().getEmail().getDefendantClaimIssued(),
            aggregateParams(claim),
            claim.getReferenceNumber()
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        return parameters;
    }
}
