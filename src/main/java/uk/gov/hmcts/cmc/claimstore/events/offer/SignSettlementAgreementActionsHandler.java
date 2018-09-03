package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationToDefendantService;

@Component
public class SignSettlementAgreementActionsHandler {

    private final NotificationToDefendantService notificationToDefendantService;

    @Autowired
    public SignSettlementAgreementActionsHandler(NotificationToDefendantService notificationToDefendantService) {
        this.notificationToDefendantService = notificationToDefendantService;
    }

    @EventListener
    public void sendNotificationToDefendant(SignSettlementAgreementEvent event) {
        this.notificationToDefendantService.notifyDefendant(event.getClaim());
    }
}
