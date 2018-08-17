package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationToDefendantService;

@Component
public class ClaimantResponseActionsHandler {

    private final NotificationToDefendantService notificationService;

    public ClaimantResponseActionsHandler(NotificationToDefendantService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void sendNotificationToDefendant(ClaimantResponseEvent event) {
        this.notificationService.notifyDefendant(event.getClaim());
    }
}
