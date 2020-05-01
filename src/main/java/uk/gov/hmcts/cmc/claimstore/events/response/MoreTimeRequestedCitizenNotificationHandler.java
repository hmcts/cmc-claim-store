package uk.gov.hmcts.cmc.claimstore.events.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.MoreTimeRequestedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Component
public class MoreTimeRequestedCitizenNotificationHandler {

    private static final String REFERENCE_TEMPLATE = "more-time-requested-notification-to-%s-%s";

    private final MoreTimeRequestedNotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public MoreTimeRequestedCitizenNotificationHandler(
        MoreTimeRequestedNotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @EventListener
    public void sendNotifications(MoreTimeRequestedEvent event) {
        sendNotificationToClaimant(event);
        sendNotificationToDefendant(event);
    }

    private void sendNotificationToDefendant(MoreTimeRequestedEvent event) {
        if (event.getDefendantEmail() != null) {
            notificationService.sendMail(
                event.getDefendantEmail(),
                notificationsProperties.getTemplates().getEmail().getDefendantMoreTimeRequested(),
                prepareNotificationParameters(event),
                referenceForDefendant(event.getClaim().getReferenceNumber())
            );
        }
    }

    private void sendNotificationToClaimant(MoreTimeRequestedEvent event) {
        notificationService.sendMail(
            event.getClaim().getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantMoreTimeRequested(),
            prepareNotificationParameters(event),
            String.format(REFERENCE_TEMPLATE, "claimant", event.getClaim().getReferenceNumber())
        );
    }

    private Map<String, String> prepareNotificationParameters(MoreTimeRequestedEvent event) {

        Claim claim = event.getClaim();

        Map<String, String> parameters = new HashMap<>();
        parameters.put("claimReferenceNumber", claim.getReferenceNumber());
        parameters.put("claimantType", PartyUtils.getType(claim.getClaimData().getClaimant()));
        parameters.put("claimantName", claim.getClaimData().getClaimant().getName());
        parameters.put("defendantName", claim.getClaimData().getDefendant().getName());
        parameters.put("responseDeadline", formatDate(event.getNewResponseDeadline()));
        parameters.put("frontendBaseUrl", notificationsProperties.getFrontendBaseUrl());
        return parameters;
    }
}
