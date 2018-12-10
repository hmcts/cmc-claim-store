package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.SettlementAgreementRejectedStaffNotificationService;

@Component
public class SettlementAgreementRejectionStaffNotificationHandler {

    private final SettlementAgreementRejectedStaffNotificationService notificationService;

    @Autowired
    public SettlementAgreementRejectionStaffNotificationHandler(
        SettlementAgreementRejectedStaffNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onSettlementAgreementRejected(SettlementAgreementRejectedEvent event) {
        notificationService.notifySettlementRejected(
            event.getClaim()
        );
    }
}
