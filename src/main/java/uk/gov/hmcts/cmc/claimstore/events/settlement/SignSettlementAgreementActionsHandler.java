package uk.gov.hmcts.cmc.claimstore.events.settlement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.notifications.SettlementAgreementNotificationService;

@Component
public class SignSettlementAgreementActionsHandler {

    private final SettlementAgreementNotificationService settlementAgreementNotificationService;

    @Autowired
    public SignSettlementAgreementActionsHandler(
        SettlementAgreementNotificationService notificationToDefendantService) {
        this.settlementAgreementNotificationService = notificationToDefendantService;
    }

    @EventListener
    public void sendNotificationToDefendant(SignSettlementAgreementEvent event) {
        this.settlementAgreementNotificationService.notifyDefendant(event.getClaim());
    }

    @EventListener
    public void sendNotificationToClaimant(SignSettlementAgreementEvent event) {
        this.settlementAgreementNotificationService.notifyClaimant(event.getClaim());
    }
}
