package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.notifications.CCJRequestedNotificationService;

@Component
public class CCJRequestedCitizenActionsHandler {
    private final CCJRequestedNotificationService ccjRequestedNotificationService;

    @Autowired
    public CCJRequestedCitizenActionsHandler(
        final CCJRequestedNotificationService ccjRequestedNotificationService
    ) {
        this.ccjRequestedNotificationService = ccjRequestedNotificationService;
    }

    @EventListener
    public void sendClaimantNotification(final CountyCourtJudgmentRequestedEvent event) {
        final Claim claim = event.getClaim();

        ccjRequestedNotificationService.notifyClaimant(claim);
    }

    @EventListener
    public void sendDefendantNotification(final CountyCourtJudgmentRequestedEvent event) {
        final Claim claim = event.getClaim();

        claim.getClaimData().getDefendant().getEmail()
            .ifPresent(defendantEmail -> ccjRequestedNotificationService.notifyDefendant(claim));
    }
}
