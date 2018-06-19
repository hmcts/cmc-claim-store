package uk.gov.hmcts.cmc.claimstore.events.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.notifications.DefendantResponseNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

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
        Claim claim = event.getClaim();
        if (isFullAdmission(claim)) {
            return;
        }
        defendantResponseNotificationService.notifyDefendant(
            claim,
            event.getUserEmail(),
            referenceForDefendant(claim.getReferenceNumber())
        );
    }

    private boolean isFullAdmission(Claim claim) {
        ResponseType responseType = claim.getResponse().orElseThrow(IllegalArgumentException::new).getResponseType();
        return responseType == ResponseType.FULL_ADMISSION;
    }

    @EventListener
    public void notifyClaimantResponse(DefendantResponseEvent event) {
        Claim claim = event.getClaim();
        if (isFullAdmission(claim)) {
            return;
        }
        defendantResponseNotificationService.notifyClaimant(
            claim,
            referenceForClaimant(claim.getReferenceNumber())
        );
    }
}
