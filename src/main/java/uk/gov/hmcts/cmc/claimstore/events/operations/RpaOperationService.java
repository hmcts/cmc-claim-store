package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.claimstore.rpa.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Arrays;

@Component
public class RpaOperationService {

    private final ClaimIssuedNotificationService notificationService;
    private final ClaimCreationEventsStatusService eventsStatusService;

    @Autowired
    public RpaOperationService(
        @Qualifier("rpa/claim-issued-notification-service") ClaimIssuedNotificationService notificationService,
        ClaimCreationEventsStatusService eventsStatusService
    ) {
        this.notificationService = notificationService;
        this.eventsStatusService = eventsStatusService;
    }

    @LogExecutionTime
    public Claim notify(Claim claim, String authorisation, PDF... documents) {

        notificationService.notifyRobotics(claim, Arrays.asList(documents));

        return eventsStatusService.updateClaimOperationCompletion(authorisation, claim, CaseEvent.SENDING_RPA);
    }
}
