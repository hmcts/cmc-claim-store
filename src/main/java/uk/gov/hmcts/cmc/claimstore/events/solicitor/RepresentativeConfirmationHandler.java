package uk.gov.hmcts.cmc.claimstore.events.solicitor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_REPRESENTATIVE;

@Component
public class RepresentativeConfirmationHandler {

    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;

    public RepresentativeConfirmationHandler(
        ClaimIssuedNotificationService claimIssuedNotificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @EventListener
    @LogExecutionTime
    public void sendConfirmation(RepresentedClaimIssuedEvent event) {
        Claim claim = event.getClaim();

        claimIssuedNotificationService.sendMail(
            claim,
            event.getRepresentativeEmail(),
            null,
            getEmailTemplates().getRepresentativeClaimIssued(),
            "representative-issue-notification-" + claim.getReferenceNumber(),
            event.getRepresentativeName()
                .orElseThrow(() -> new IllegalArgumentException(MISSING_REPRESENTATIVE)));
    }

    private EmailTemplates getEmailTemplates() {
        NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
