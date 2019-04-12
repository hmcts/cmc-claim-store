package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedCitizenActionsHandler;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_eventOperations_enabled")
public class DefendantOperationService {

    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public DefendantOperationService(
        ClaimIssuedNotificationService claimIssuedNotificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
    }

    public Claim notify(Claim claim, String pin, String submitterName, String authorisation) {
        //TODO check claim if operation already complete, if yes return claim else

        if (!claim.getClaimData().isClaimantRepresented()) {
            claim.getClaimData().getDefendant().getEmail().ifPresent(defendantEmail ->
                claimIssuedNotificationService.sendMail(
                    claim,
                    defendantEmail,
                    pin,
                    notificationsProperties.getTemplates().getEmail().getDefendantClaimIssued(),
                    "defendant-issue-notification-" + claim.getReferenceNumber(),
                    submitterName
                ));
        }

        //TODO update claim and return updated claim, below is placeholder
        return claim;
    }
}
