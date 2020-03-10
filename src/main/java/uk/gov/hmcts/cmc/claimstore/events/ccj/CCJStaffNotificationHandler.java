package uk.gov.hmcts.cmc.claimstore.events.ccj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.CCJStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.InterlocutoryJudgmentStaffNotificationService;

@Component
public class CCJStaffNotificationHandler {

    private final CCJStaffNotificationService ccjStaffNotificationService;
    private final InterlocutoryJudgmentStaffNotificationService interlocutoryJudgmentStaffNotificationService;

    @Autowired
    public CCJStaffNotificationHandler(
        CCJStaffNotificationService ccjStaffNotificationService,
        InterlocutoryJudgmentStaffNotificationService interlocutoryJudgmentStaffNotificationService
    ) {
        this.ccjStaffNotificationService = ccjStaffNotificationService;
        this.interlocutoryJudgmentStaffNotificationService = interlocutoryJudgmentStaffNotificationService;
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

    @EventListener
    public void onInterlocutoryJudgmentEvent(InterlocutoryJudgmentEvent event) {
        this.interlocutoryJudgmentStaffNotificationService.notifyStaffInterlocutoryJudgmentSubmitted(event.getClaim());
    }
}
