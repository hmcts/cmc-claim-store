package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.settlement.RejectSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.services.staff.RejectSettlementAgreementStaffNotificationService;

@Component
public class RejectSettlementAgreementStaffNotificationHandler {

    private final RejectSettlementAgreementStaffNotificationService notificationService;

    @Autowired
    public RejectSettlementAgreementStaffNotificationHandler(
        RejectSettlementAgreementStaffNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onSettlementAgreementRejected(RejectSettlementAgreementEvent event) {
        notificationService.notifySettlementRejected(event.getClaim());
    }
}
