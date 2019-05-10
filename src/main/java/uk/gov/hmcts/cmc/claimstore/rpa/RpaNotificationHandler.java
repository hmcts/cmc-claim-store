package uk.gov.hmcts.cmc.claimstore.rpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;

import static java.util.Objects.requireNonNull;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "emailToStaff", havingValue = "true")
public class RpaNotificationHandler {
    private final ClaimIssuedNotificationService rpaNotificationService;

    @Autowired
    public RpaNotificationHandler(
        @Qualifier("rpa/claim-issued-notification-service") ClaimIssuedNotificationService rpaNotificationService
    ) {
        this.rpaNotificationService = rpaNotificationService;
    }

    @EventListener
    @LogExecutionTime
    public void notifyRobotOfClaimIssue(DocumentGeneratedEvent event) {
        requireNonNull(event);

        rpaNotificationService.notifyRobotics(event.getClaim(), event.getDocuments());
    }
}
