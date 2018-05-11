package uk.gov.hmcts.cmc.claimstore.events.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.MoreTimeRequestedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff")
public class MoreTimeRequestedStaffNotificationHandler {

    private static final String REFERENCE_TEMPLATE = "more-time-requested-notification-to-%s-%s";

    private final MoreTimeRequestedNotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final StaffEmailProperties staffEmailProperties;

    @Autowired
    public MoreTimeRequestedStaffNotificationHandler(
        MoreTimeRequestedNotificationService notificationService,
        NotificationsProperties notificationsProperties,
        StaffEmailProperties staffEmailProperties
    ) {

        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.staffEmailProperties = staffEmailProperties;
    }

    @EventListener
    public void sendNotifications(MoreTimeRequestedEvent event) {
        notificationService.sendMail(
            staffEmailProperties.getRecipient(),
            notificationsProperties.getTemplates().getEmail().getStaffMoreTimeRequested(),
            prepareNotificationParameters(event),
            String.format(REFERENCE_TEMPLATE, "staff", event.getClaim().getReferenceNumber())
        );
    }

    private Map<String, String> prepareNotificationParameters(MoreTimeRequestedEvent event) {

        Claim claim = event.getClaim();

        Map<String, String> parameters = new HashMap<>();
        parameters.put("claimReferenceNumber", claim.getReferenceNumber());
        parameters.put("claimantName", claim.getClaimData().getClaimant().getName());
        parameters.put("defendantName", claim.getClaimData().getDefendant().getName());
        parameters.put("responseDeadline", formatDate(event.getNewResponseDeadline()));
        parameters.put("claimantType", PartyUtils.getType(claim.getClaimData().getClaimant()));
        return parameters;
    }
}
