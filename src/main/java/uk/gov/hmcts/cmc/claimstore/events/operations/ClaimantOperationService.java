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
        //TODO check claim if operation already complete, if yes return claim else

        claimIssuedNotificationService.sendMail(
            claim,
            claim.getSubmitterEmail(),
            null,
            notificationsProperties.getTemplates().getEmail().getClaimantClaimIssued(),
            "claimant-issue-notification-" + claim.getReferenceNumber(),
            submitterName
        );

        //TODO update claim and return updated claim, below is placeholder
        return claim;
    }

    @LogExecutionTime
    public Claim confirmRepresentative(
        Claim claim,
        String submitterName,
        String representativeEmail,
        String authorisation
    ) {
        //TODO check claim if operation already complete, if yes return claim else

        claimIssuedNotificationService.sendMail(
            claim,
            representativeEmail,
            null,
            notificationsProperties.getTemplates().getEmail().getRepresentativeClaimIssued(),
            "representative-issue-notification-" + claim.getReferenceNumber(),
            submitterName
        );

        //TODO update claim and return updated claim, below is placeholder
        return claim;
    }
}
