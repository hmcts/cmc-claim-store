package uk.gov.hmcts.cmc.claimstore.events.ccj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.ReDeterminationEvent;
import uk.gov.hmcts.cmc.claimstore.services.staff.CCJStaffNotificationService;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff")
public class CCJStaffNotificationHandler {

    private final CCJStaffNotificationService ccjStaffNotificationService;

    @Autowired
    public CCJStaffNotificationHandler(CCJStaffNotificationService ccjStaffNotificationService) {
        this.ccjStaffNotificationService = ccjStaffNotificationService;
    }

    @EventListener
    public void onDefaultJudgmentRequestSubmitted(CountyCourtJudgmentEvent event) {
        this.ccjStaffNotificationService.notifyStaffCCJRequestSubmitted(event.getClaim());
    }

    @EventListener
    public void onRedeterminationRequest(ReDeterminationEvent event) {
        this.ccjStaffNotificationService
            .notifyStaffCCJReDeterminationRequest(event.getClaim(), event.getSubmitterName());
    }
}
