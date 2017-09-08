package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.notifications.MoreTimeRequestedNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.PartyTypeContentProvider;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Component
public class MoreTimeRequestedStaffNotificationHandler {

    private static final String REFERENCE_TEMPLATE = "more-time-requested-notification-to-%s-%s";
    private static final String STAFF = "staff";
    private static final String CLAIMANT_TYPE = "claimantType";
    private static final String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    private static final String CLAIMANT_NAME = "claimantName";
    private static final String DEFENDANT_NAME = "defendantName";
    private static final String RESPONSE_DEADLINE = "responseDeadline";

    private final MoreTimeRequestedNotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final StaffEmailProperties staffEmailProperties;

    @Autowired
    public MoreTimeRequestedStaffNotificationHandler(
        final MoreTimeRequestedNotificationService notificationService,
        final NotificationsProperties notificationsProperties,
        final StaffEmailProperties staffEmailProperties
    ) {

        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.staffEmailProperties = staffEmailProperties;
    }

    @EventListener
    public void sendNotifications(final MoreTimeRequestedEvent event) {
        notificationService.sendMail(
            staffEmailProperties.getRecipient(),
            notificationsProperties.getTemplates().getEmail().getStaffMoreTimeRequested(),
            prepareNotificationParameters(event),
            String.format(REFERENCE_TEMPLATE, STAFF, event.getClaim().getReferenceNumber())
        );
    }

    private Map<String, String> prepareNotificationParameters(final MoreTimeRequestedEvent event) {

        Claim claim = event.getClaim();

        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(RESPONSE_DEADLINE, formatDate(event.getNewResponseDeadline()));
        parameters.put(CLAIMANT_TYPE, PartyTypeContentProvider.getType(claim.getClaimData().getClaimant()));
        return parameters;
    }
}
