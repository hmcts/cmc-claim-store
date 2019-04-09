package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedCitizenActionsHandler;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_eventOperations_enabled")
public class ClaimantOperationService {
    private final ClaimIssuedCitizenActionsHandler claimIssuedCitizenActionsHandler;

    @Autowired
    public ClaimantOperationService(ClaimIssuedCitizenActionsHandler claimIssuedCitizenActionsHandler) {
        this.claimIssuedCitizenActionsHandler = claimIssuedCitizenActionsHandler;
    }

    public Claim notify(Claim claim, String pin, String submitterName, String authorisation) {
        //TODO check claim if operation already complete, if yes return claim else

        CitizenClaimIssuedEvent issuedEvent = new CitizenClaimIssuedEvent(claim, pin, submitterName, authorisation);
        claimIssuedCitizenActionsHandler.sendClaimantNotification(issuedEvent);

        //TODO update claim and return updated claim, below is placeholder
        return claim;
    }
}
