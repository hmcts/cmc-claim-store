package uk.gov.hmcts.cmc.claimstore.events.ccj;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.notifications.CCJNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;

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
        CountyCourtJudgmentType countyCourtJudgmentType = claim.getCountyCourtJudgment().getCcjType();
        switch (countyCourtJudgmentType) {
            case DEFAULT:
                ccjNotificationService.notifyClaimantForCCJRequest(claim);
                break;
            case ADMISSIONS:
                ccjNotificationService.notifyClaimantForCCJRequest(claim);
                ccjNotificationService.notifyDefendantForCCJRequested(claim);
                break;
            case DETERMINATION:
                // TODO: Action to be taken
                break;
            default:
                throw new IllegalArgumentException("Incorrect event provided: "
                    + countyCourtJudgmentType);
        }
    }
}
