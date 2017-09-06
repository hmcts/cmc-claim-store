package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.notifications.DefendantResponseNotificationService;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ResponseSubmitted.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ResponseSubmitted.referenceForDefendant;

@Component
public class DefendantResponseCitizenNotificationsHandler {
    private final DefendantResponseNotificationService defendantResponseNotificationService;

    @Autowired
    public DefendantResponseCitizenNotificationsHandler(
        final DefendantResponseNotificationService defendantResponseNotificationService) {
        this.defendantResponseNotificationService = defendantResponseNotificationService;
    }

    @EventListener
    public void notifyDefendantResponse(final DefendantResponseEvent event) {
        defendantResponseNotificationService.notifyDefendant(
            event.getClaim(),
            event.getUserEmail(),
            referenceForDefendant(event.getClaim().getReferenceNumber())
        );
    }

    @EventListener
    public void notifyClaimantResponse(final DefendantResponseEvent event) {
        Claim claim = event.getClaim();

        defendantResponseNotificationService.notifyClaimant(
            claim,
            event.getDefendantResponse(),
            claim.getSubmitterEmail(),
            referenceForClaimant(claim.getReferenceNumber())
        );
    }
}
