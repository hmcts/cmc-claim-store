package uk.gov.hmcts.cmc.claimstore.rpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;

import static java.util.Objects.requireNonNull;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff")
public class RpaNotificationHandler {
    private final ClaimIssueNotificationService claimIssueNotificationService;

    @Autowired
    public RpaNotificationHandler(ClaimIssueNotificationService claimIssueNotificationService) {
        this.claimIssueNotificationService = claimIssueNotificationService;
    }

    @EventListener
    @LogExecutionTime
    public void notifyRobotOfClaimIssue(DocumentGeneratedEvent event) {
        requireNonNull(event);

        claimIssueNotificationService.notifyRobotics(event.getClaim(), event.getDocuments());
    }
}
