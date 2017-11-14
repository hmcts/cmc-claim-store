package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.notifications.CCJRequestedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

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
}
