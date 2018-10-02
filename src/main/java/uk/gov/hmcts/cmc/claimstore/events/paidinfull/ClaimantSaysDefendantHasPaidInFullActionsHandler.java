package uk.gov.hmcts.cmc.claimstore.events.paidinfull;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;

public class ClaimantSaysDefendantHasPaidInFullActionsHandler {
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public ClaimantSaysDefendantHasPaidInFullActionsHandler(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }
}
