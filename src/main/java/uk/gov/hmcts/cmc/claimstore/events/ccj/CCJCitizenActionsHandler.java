package uk.gov.hmcts.cmc.claimstore.events.ccj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.notifications.CCJNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
public class CCJCitizenActionsHandler {
    private final CCJNotificationService ccjNotificationService;

    @Autowired
    public CCJCitizenActionsHandler(CCJNotificationService ccjNotificationService) {
        this.ccjNotificationService = ccjNotificationService;
    }

    @EventListener
    public void sendNotification(CountyCourtJudgmentEvent event) {
        Claim claim = event.getClaim();
        switch (event.getCountyCourtJudgmentType()) {
            case DEFAULT:
                ccjNotificationService.notifyClaimantForCCJRequest(claim);
                break;
            case ADMISSIONS:
            case DETERMINATION:
                ccjNotificationService.notifyClaimantForCCJRequest(claim);
                ccjNotificationService.notifyDefendantForCCJIssue(claim);
                break;
            default:
                throw new IllegalArgumentException("Incorrect event provided: "
                    + event.getCountyCourtJudgmentType().name());
        }
    }
}
