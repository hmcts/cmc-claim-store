package uk.gov.hmcts.cmc.claimstore.events.ccj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.CCJStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.InterlocutoryJudgmentStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.SaveClaimantResponseDocumentService;

@Component
public class CCJStaffNotificationHandler {

    private final CCJStaffNotificationService ccjStaffNotificationService;
    private final InterlocutoryJudgmentStaffNotificationService interlocutoryJudgmentStaffNotificationService;
    private final SaveClaimantResponseDocumentService saveClaimantResponseDocumentService;
    private final boolean staffEmailEnabled;

    @Autowired
    public CCJStaffNotificationHandler(
        CCJStaffNotificationService ccjStaffNotificationService,
        InterlocutoryJudgmentStaffNotificationService interlocutoryJudgmentStaffNotificationService,
        SaveClaimantResponseDocumentService saveClaimantResponseDocumentService,
        @Value("${feature_toggles.staff_emails_enabled}") boolean staffEmailEnabled
    ) {
        this.ccjStaffNotificationService = ccjStaffNotificationService;
        this.interlocutoryJudgmentStaffNotificationService = interlocutoryJudgmentStaffNotificationService;
        this.saveClaimantResponseDocumentService = saveClaimantResponseDocumentService;
        this.staffEmailEnabled = staffEmailEnabled;
    }

    @EventListener
    public void onDefaultJudgmentRequestSubmitted(CountyCourtJudgmentEvent event) {
        if (staffEmailEnabled) {
            this.ccjStaffNotificationService.notifyStaffCCJRequestSubmitted(event.getClaim());
        } else {
            this.saveClaimantResponseDocumentService.getAndSaveDocumentToCcd(event.getClaim());
        }
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
