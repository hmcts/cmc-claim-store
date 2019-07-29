package uk.gov.hmcts.cmc.claimstore.events.revieworder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.ReviewOrderStaffNotificationService;

@Component
public class ReviewOrderStaffNotificationHandler {

    private final ReviewOrderStaffNotificationService reviewOrderStaffNotificationService;

    @Autowired
    public ReviewOrderStaffNotificationHandler(
        ReviewOrderStaffNotificationService reviewOrderStaffNotificationService
    ) {
        this.reviewOrderStaffNotificationService = reviewOrderStaffNotificationService;
    }

    @EventListener
    public void onReviewOrderEvent(ReviewOrderEvent event) {
        this.reviewOrderStaffNotificationService.notifyForReviewOrder(event.getClaim());
    }
}
