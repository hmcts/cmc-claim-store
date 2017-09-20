package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.CCJStaffNotificationService;

@Component
public class CountyCourtJudgmentStaffNotificationHandler {

    private final CCJStaffNotificationService ccjStaffNotificationService;

    @Autowired
    public CountyCourtJudgmentStaffNotificationHandler(
        CCJStaffNotificationService ccjStaffNotificationService) {
        this.ccjStaffNotificationService = ccjStaffNotificationService;
    }

    @EventListener
    public void onDefaultJudgmentRequestSubmitted(final CountyCourtJudgmentSubmittedEvent event) {
        this.ccjStaffNotificationService.notifyStaffCCJRequestSubmitted(
            event.getClaim()
        );
    }
}
