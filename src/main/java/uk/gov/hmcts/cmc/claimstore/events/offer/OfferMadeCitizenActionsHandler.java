package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.OfferResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Component
public class OfferMadeCitizenActionsHandler {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OfferResponseDeadlineCalculator offerResponseDeadlineCalculator;

    @Autowired
    public OfferMadeCitizenActionsHandler(
        NotificationService notificationService,
        OfferResponseDeadlineCalculator offerResponseDeadlineCalculator,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.offerResponseDeadlineCalculator = offerResponseDeadlineCalculator;
        this.notificationsProperties = notificationsProperties;
    }

    @EventListener
    public void sendClaimantNotification(OfferMadeEvent event) {
        Claim claim = event.getClaim();

        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantOfferMade(),
            aggregateParams(claim),
            NotificationReferenceBuilder.OfferMade.referenceForClaimant(claim.getReferenceNumber())
        );
    }

    @EventListener
    public void sendDefendantNotification(OfferMadeEvent event) {
        Claim claim = event.getClaim();

        notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getDefendantOfferMade(),
            aggregateParams(claim),
            NotificationReferenceBuilder.OfferMade.referenceForDefendant(claim.getReferenceNumber())
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put("responseForOfferDeadline",
            Formatting.formatDate(
                offerResponseDeadlineCalculator.calculateOfferResponseDeadline(LocalDateTime.now())
            )
        );

        return parameters;
    }
}
