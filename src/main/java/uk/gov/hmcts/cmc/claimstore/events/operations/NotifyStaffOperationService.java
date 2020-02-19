package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
public class NotifyStaffOperationService {
    private final ClaimCreationEventsStatusService eventsStatusService;

    @Autowired
    public NotifyStaffOperationService(ClaimCreationEventsStatusService eventsStatusService) {
        this.eventsStatusService = eventsStatusService;
    }

    public Claim notify(Claim claim, String authorisation) {
        return eventsStatusService.updateClaimOperationCompletion(authorisation, claim,
            CaseEvent.PIN_GENERATION_OPERATIONS);
    }
}
