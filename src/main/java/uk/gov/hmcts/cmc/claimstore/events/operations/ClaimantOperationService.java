package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled")
public class ClaimantOperationService {
    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public ClaimantOperationService(
        ClaimIssuedNotificationService claimIssuedNotificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
    }

    public Claim notifyCitizen(Claim claim, String submitterName, String authorisation) {
        claimIssuedNotificationService.sendMail(
            claim,
            claim.getSubmitterEmail(),
            null,
            notificationsProperties.getTemplates().getEmail().getClaimantClaimIssued(),
            "claimant-issue-notification-" + claim.getReferenceNumber(),
            submitterName
        );

        return claim;
    }

    @LogExecutionTime
    public Claim confirmRepresentative(
        Claim claim,
        String submitterName,
        String representativeEmail,
        String authorisation
    ) {
        claimIssuedNotificationService.sendMail(
            claim,
            representativeEmail,
            null,
            notificationsProperties.getTemplates().getEmail().getRepresentativeClaimIssued(),
            "representative-issue-notification-" + claim.getReferenceNumber(),
            submitterName
        );

        return claim;
    }
}
