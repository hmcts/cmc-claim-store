package uk.gov.hmcts.cmc.claimstore.events.solicitor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Optional;

@Component
public class RepresentativeConfirmationHandler {

    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;

    public RepresentativeConfirmationHandler(
        final ClaimIssuedNotificationService claimIssuedNotificationService,
        final NotificationsProperties notificationsProperties
    ) {
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @EventListener
    public void sendConfirmation(RepresentedClaimIssuedEvent event) {
        final Claim claim = event.getClaim();

        claimIssuedNotificationService.sendMail(
            claim,
            event.getRepresentativeEmail(),
            Optional.empty(),
            getEmailTemplates().getRepresentativeClaimIssued(),
            "representative-issue-notification-" + claim.getReferenceNumber(),
            event.getRepresentativeName().orElseThrow(IllegalArgumentException::new));
    }

    private EmailTemplates getEmailTemplates() {
        final NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
