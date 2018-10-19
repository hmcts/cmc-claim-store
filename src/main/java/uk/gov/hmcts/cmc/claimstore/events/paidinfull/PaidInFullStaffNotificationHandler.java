package uk.gov.hmcts.cmc.claimstore.events.paidinfull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.PaidInFullStaffNotificationService;

@Component
public class PaidInFullStaffNotificationHandle {

    private final PaidInFullStaffNotificationService paidInFullStaffNotificationService;

    @Autowired
    public PaidInFullStaffNotificationHandle(PaidInFullStaffNotificationService paidInFullStaffNotificationService) {
        this.paidInFullStaffNotificationService = paidInFullStaffNotificationService;
    }

    @EventListener
    public void onPaidInFullEvent(PaidInFullEvent event) {
        this.paidInFullStaffNotificationService.notifyPaidInFull(event.getClaim());
    }
}
