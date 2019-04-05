package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentGenerator;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Async("threadPoolTaskExecutor")
public class ClaimIssueOperationHandler {

    private final DocumentGenerator documentGenerator;
    private final ClaimIssuedCitizenActionsHandler claimIssuedCitizenActionsHandler;

    public ClaimIssueOperationHandler(
        DocumentGenerator documentGenerator,
        ClaimIssuedCitizenActionsHandler claimIssuedCitizenActionsHandler

    ) {
        this.documentGenerator = documentGenerator;
        this.claimIssuedCitizenActionsHandler = claimIssuedCitizenActionsHandler;
    }

    @EventListener
    public void operationHandler(CitizenClaimIssuedEvent citizenClaimIssuedEvent) {
         documentGenerator.generateForNonRepresentedClaim(citizenClaimIssuedEvent);
        claimIssuedCitizenActionsHandler.sendClaimantNotification(citizenClaimIssuedEvent);
        claimIssuedCitizenActionsHandler.sendDefendantNotification(citizenClaimIssuedEvent);
    }
}
