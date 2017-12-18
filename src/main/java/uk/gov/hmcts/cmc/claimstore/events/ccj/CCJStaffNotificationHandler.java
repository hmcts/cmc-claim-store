package uk.gov.hmcts.cmc.claimstore.events.ccj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.CCJStaffNotificationService;

@Component
public class CCJStaffNotificationHandler {

    private final CCJStaffNotificationService ccjStaffNotificationService;

    @Autowired
    public CCJStaffNotificationHandler(CCJStaffNotificationService ccjStaffNotificationService) {
        this.ccjStaffNotificationService = ccjStaffNotificationService;
    }

    @EventListener
    public void onDefaultJudgmentRequestSubmitted(CountyCourtJudgmentRequestedEvent event) {
        this.ccjStaffNotificationService.notifyStaffCCJRequestSubmitted(
            event.getClaim()
        );
    }
}
