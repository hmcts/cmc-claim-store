package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentativeConfirmationHandler;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_eventOperations_enabled")
public class RepresentativeOperationService {
    private final RepresentativeConfirmationHandler representativeConfirmationHandler;

    @Autowired
    public RepresentativeOperationService(RepresentativeConfirmationHandler representativeConfirmationHandler) {
        this.representativeConfirmationHandler = representativeConfirmationHandler;
    }

    public Claim notify(Claim claim, String submitterName, String authorisation) {
        //TODO check claim if operation already complete, if yes return claim else

        RepresentedClaimIssuedEvent representedClaimIssuedEvent = new RepresentedClaimIssuedEvent(
            claim,
            submitterName,
            authorisation
        );

        representativeConfirmationHandler.sendConfirmation(representedClaimIssuedEvent);

        //TODO update claim and return updated claim, below is placeholder
        return claim;
    }
}
