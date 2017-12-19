package uk.gov.hmcts.cmc.claimstore.events.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.notifications.DefendantResponseNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ResponseSubmitted.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ResponseSubmitted.referenceForDefendant;

@Component
public class DefendantResponseCitizenNotificationsHandler {
    private final DefendantResponseNotificationService defendantResponseNotificationService;

    @Autowired
    public DefendantResponseCitizenNotificationsHandler(
        DefendantResponseNotificationService defendantResponseNotificationService) {
        this.defendantResponseNotificationService = defendantResponseNotificationService;
    }

    @EventListener
    public void notifyDefendantResponse(DefendantResponseEvent event) {
        defendantResponseNotificationService.notifyDefendant(
            event.getClaim(),
            event.getUserEmail(),
            referenceForDefendant(event.getClaim().getReferenceNumber())
        );
    }

    @EventListener
    public void notifyClaimantResponse(DefendantResponseEvent event) {
        Claim claim = event.getClaim();

        defendantResponseNotificationService.notifyClaimant(
            claim,
            referenceForClaimant(claim.getReferenceNumber())
        );
    }
}
