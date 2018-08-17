package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import com.google.common.collect.ImmutableMap;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ClaimantResponse.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Component
public class ClaimantResponseActionsHandler {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    public ClaimantResponseActionsHandler(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @EventListener
    public void sendNotificationToDefendant(ClaimantResponseEvent event) {
        Claim claim = event.getClaim();
        this.notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getResponseByClaimantEmailToDefendant(),
            aggregateParams(claim),
            referenceForDefendant(claim.getReferenceNumber())
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {
        return new ImmutableMap.Builder<String, String>()
            .put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName())
            .put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl())
            .put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber())
            .build();
    }
}
