package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.rpa.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Arrays;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_event_operations_enabled")
public class RpaOperationService {

    private final ClaimIssuedNotificationService notificationService;

    @Autowired
    public RpaOperationService(
        @Qualifier("rpa/claim-issued-notification-service") ClaimIssuedNotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    public Claim notify(Claim claim, String authorisation, PDF... documents) {
        //TODO check claim if operation already complete, if yes return claim else

        notificationService.notifyRobotics(claim, Arrays.asList(documents));

        //TODO update claim and return updated claim, below is placeholder
        return claim;
    }
}
