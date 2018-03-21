package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Component
public class OfferRespondedCitizenActionsHandler {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public OfferRespondedCitizenActionsHandler(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @EventListener
    public void sendNotificationToClaimantOnOfferAcceptedByClaimant(OfferAcceptedEvent event) {
        Claim claim = event.getClaim();

        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getOfferAcceptedByClaimantEmailToClaimant(),
            aggregateParams(claim),
            NotificationReferenceBuilder.OfferAccepted.referenceForClaimant(claim.getReferenceNumber())
        );
    }

    @EventListener
    public void sendNotificationToDefendantOnOfferAcceptedByClaimant(OfferAcceptedEvent event) {
        Claim claim = event.getClaim();

        notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getOfferAcceptedByClaimantEmailToDefendant(),
            aggregateParams(claim),
            NotificationReferenceBuilder.OfferAccepted.referenceForDefendant(claim.getReferenceNumber())
        );
    }

    @EventListener
    public void sendNotificationToClaimantOnOfferRejectedByClaimant(OfferRejectedEvent event) {
        Claim claim = event.getClaim();

        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getOfferRejectedByClaimantEmailToClaimant(),
            aggregateParams(claim),
            NotificationReferenceBuilder.OfferRejected.referenceForClaimant(claim.getReferenceNumber())
        );
    }

    @EventListener
    public void sendNotificationToDefendantOnOfferRejectedByClaimant(OfferRejectedEvent event) {
        Claim claim = event.getClaim();

        notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getOfferRejectedByClaimantEmailToDefendant(),
            aggregateParams(claim),
            NotificationReferenceBuilder.OfferRejected.referenceForDefendant(claim.getReferenceNumber())
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
