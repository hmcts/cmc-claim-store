package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
public class ClaimIssuedCitizenActionsHandler {

    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public ClaimIssuedCitizenActionsHandler(
        ClaimIssuedNotificationService claimIssuedNotificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @EventListener
    public void sendClaimantNotification(CitizenClaimIssuedEvent event) {
        Claim claim = event.getClaim();

        claimIssuedNotificationService.sendMail(
            claim,
            claim.getSubmitterEmail(),
            null,
            getEmailTemplates().getClaimantClaimIssued(),
            "claimant-issue-notification-" + claim.getReferenceNumber(),
            event.getSubmitterName()
        );
    }

    @EventListener
    public void sendDefendantNotification(CitizenClaimIssuedEvent event) {
        Claim claim = event.getClaim();

        if (!claim.getClaimData().isClaimantRepresented()) {
            claim.getClaimData().getDefendant().getEmail()
                .ifPresent(defendantEmail ->
                    claimIssuedNotificationService.sendMail(
                        claim,
                        defendantEmail,
                        event.getPin(),
                        getEmailTemplates().getDefendantClaimIssued(),
                        "defendant-issue-notification-" + claim.getReferenceNumber(),
                        event.getSubmitterName()
                    ));
        }
    }

    private EmailTemplates getEmailTemplates() {
        NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
