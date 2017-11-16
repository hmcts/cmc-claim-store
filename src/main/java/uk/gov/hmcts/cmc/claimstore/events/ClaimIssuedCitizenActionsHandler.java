package uk.gov.hmcts.cmc.claimstore.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Optional;

@Component
public class ClaimIssuedCitizenActionsHandler {

    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public ClaimIssuedCitizenActionsHandler(
        final ClaimIssuedNotificationService claimIssuedNotificationService,
        final NotificationsProperties notificationsProperties
    ) {
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @EventListener
    public void sendClaimantNotification(final ClaimIssuedEvent event) {
        final Claim claim = event.getClaim();

        claimIssuedNotificationService.sendMail(
            claim,
            event.getSubmitterEmail(),
            Optional.empty(),
            getEmailTemplates().getClaimantClaimIssued(),
            "claimant-issue-notification-" + claim.getReferenceNumber(),
            event.getSubmitterName().orElseThrow(IllegalArgumentException::new)
        );
    }

    @EventListener
    public void sendDefendantNotification(final ClaimIssuedEvent event) {
        final Claim claim = event.getClaim();

        if (!claim.getClaimData().isClaimantRepresented()) {
            claim.getClaimData().getDefendant().getEmail()
                .ifPresent(defendantEmail ->
                    claimIssuedNotificationService.sendMail(
                        claim,
                        defendantEmail,
                        event.getPin(),
                        getEmailTemplates().getDefendantClaimIssued(),
                        "defendant-issue-notification-" + claim.getReferenceNumber(),
                        event.getSubmitterName().orElseThrow(IllegalArgumentException::new)
                    ));
        }
    }

    private EmailTemplates getEmailTemplates() {
        final NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
