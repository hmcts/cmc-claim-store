package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.OfferAcceptedStaffNotificationService;

@Component
public class OfferAcceptedStaffNotificationHandler {

    private final OfferAcceptedStaffNotificationService notificationService;

    @Autowired
    public OfferAcceptedStaffNotificationHandler(
        OfferAcceptedStaffNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onOfferAccepted(OfferAcceptedEvent event) {
        notificationService.notifyOfferAccepted(
            event.getClaim()
        );
    }
}
