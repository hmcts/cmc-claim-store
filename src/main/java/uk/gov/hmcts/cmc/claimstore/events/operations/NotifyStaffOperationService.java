package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Arrays;

@Component
public class NotifyStaffOperationService {
    private final ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;
    private final ClaimCreationEventsStatusService eventsStatusService;

    @Autowired
    public NotifyStaffOperationService(ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService,
                                       ClaimCreationEventsStatusService eventsStatusService) {
        this.claimIssuedStaffNotificationService = claimIssuedStaffNotificationService;
        this.eventsStatusService = eventsStatusService;
    }

    public Claim notify(Claim claim, String authorisation, PDF... documents) {

        claimIssuedStaffNotificationService.notifyStaffOfClaimIssue(claim, Arrays.asList(documents));
        return eventsStatusService.updateClaimOperationCompletion(authorisation, claim,
            CaseEvent.PIN_GENERATION_OPERATIONS);
    }
}
