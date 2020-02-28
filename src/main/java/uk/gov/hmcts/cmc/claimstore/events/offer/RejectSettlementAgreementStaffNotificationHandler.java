package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.settlement.RejectSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.services.staff.RejectSettlementAgreementStaffNotificationService;

@Component
public class RejectSettlementAgreementStaffNotificationHandler {

    private final RejectSettlementAgreementStaffNotificationService notificationService;
    private final boolean staffEmailEnabled;

    @Autowired
    public RejectSettlementAgreementStaffNotificationHandler(
        RejectSettlementAgreementStaffNotificationService notificationService,
        @Value("${feature_toggles.staff_emails_enabled}") boolean staffEmailEnabled) {
        this.notificationService = notificationService;
        this.staffEmailEnabled = staffEmailEnabled;
    }

    @EventListener
    public void onSettlementAgreementRejected(RejectSettlementAgreementEvent event) {
        if (staffEmailEnabled) {
            notificationService.notifySettlementRejected(
                event.getClaim()
            );
        }
    }
}
