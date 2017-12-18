package uk.gov.hmcts.cmc.claimstore.events.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.DefendantResponseStaffNotificationService;

@Component
public class DefendantResponseStaffNotificationHandler {

    private final DefendantResponseStaffNotificationService defendantResponseStaffNotificationService;

    @Autowired
    public DefendantResponseStaffNotificationHandler(
        DefendantResponseStaffNotificationService defendantResponseStaffNotificationService) {
        this.defendantResponseStaffNotificationService = defendantResponseStaffNotificationService;
    }

    @EventListener
    public void onDefendantResponseSubmitted(DefendantResponseEvent event) {
        defendantResponseStaffNotificationService.notifyStaffDefenceSubmittedFor(
            event.getClaim(),
            event.getUserEmail()
        );
    }
}
