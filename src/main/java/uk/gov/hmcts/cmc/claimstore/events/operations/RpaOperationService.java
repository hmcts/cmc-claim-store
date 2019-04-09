package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.rpa.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_eventOperations_enabled")
public class RpaOperationService {

    private final ClaimIssuedNotificationService notificationService;

    @Autowired
    public RpaOperationService(
        @Qualifier("rpa/claim-issued-notification-service") ClaimIssuedNotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    public Claim notify(Claim claim, String authorization, PDF... documents) {
        //TODO check claim if operation already complete, if yes return claim else

        notificationService.notifyRobotOfClaimIssue(new DocumentGeneratedEvent(claim, authorization, documents));

        //TODO update claim and return updated claim, below is placeholder
        return claim;
    }
}
