package uk.gov.hmcts.cmc.claimstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.PostClaimOrchestrationHandler;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.models.ClaimState.CREATE;

@Service
public class ClaimReissueService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClaimService claimService;
    private final UserService userService;
    private final PostClaimOrchestrationHandler postClaimOrchestrationHandler;

    public ClaimReissueService(
        ClaimService claimService,
        UserService userService,
        PostClaimOrchestrationHandler postClaimOrchestrationHandler
    ) {
        this.claimService = claimService;
        this.userService = userService;
        this.postClaimOrchestrationHandler = postClaimOrchestrationHandler;
    }

    public void getCreatedClaimsAndReIssue() {
        try {
            User user = userService.authenticateAnonymousCaseWorker();
            String authorisation = user.getAuthorisation();
            claimService.getClaimsByState(CREATE, user).forEach(claim ->
                triggerAsyncOperation(authorisation, claim)
            );
        } catch (Exception ex) {
            logger.error("Error whilst reissuing claim", ex);

        }
    }

    private void triggerAsyncOperation(String authorisation, Claim claim) {
        if (claim.getClaimData().isClaimantRepresented()) {
            String submitterName = claim.getClaimData().getClaimant()
                .getRepresentative().orElseThrow(IllegalStateException::new)
                .getOrganisationName();

            this.postClaimOrchestrationHandler.representativeIssueHandler(
                new RepresentedClaimCreatedEvent(claim, submitterName, authorisation)
            );
        } else {
            String submitterName = claim.getClaimData().getClaimant().getName();
            this.postClaimOrchestrationHandler.citizenIssueHandler(
                new CitizenClaimCreatedEvent(claim, submitterName, authorisation)
            );
        }
    }
}
