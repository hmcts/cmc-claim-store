package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_REPRESENTATIVE;

@Service
public class ClaimIssueService {

    private final Logger logger = LoggerFactory.getLogger(ClaimIssueService.class);

    private UserService userService;
    private ClaimService claimService;
    private PostClaimOrchestrationHandler postClaimOrchestrationHandler;

    @Autowired
    public ClaimIssueService(ClaimService claimService, UserService userService,
                             PostClaimOrchestrationHandler postClaimOrchestrationHandler) {
        this.userService = userService;
        this.claimService = claimService;
        this.postClaimOrchestrationHandler = postClaimOrchestrationHandler;
    }

    @LogExecutionTime
    public void issueCreatedClaims() {
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        List<Claim> createdClaims = claimService.getClaimsByState(ClaimState.CREATE, user);
        logger.info("Automated Claim Issue - Total claims in Create state (Stuck Claims): {}",
            createdClaims
                .stream()
                .filter(claim -> (claim.getReferenceNumber() != null))
                .count());
        createdClaims
            .stream()
            .filter(claim -> (claim.getReferenceNumber() != null))
            .forEach(claim -> issueClaimsPendingInCreatedState(claim, authorisation));
    }

    private void issueClaimsPendingInCreatedState(Claim claim, String authorisation) {
        if (claim.getClaimData().isClaimantRepresented()) {
            logger.info("Automated Claim Issue should not been triggered " +
                "as this is Legal Rep case which is deprecated. {}", claim.getReferenceNumber());
        } else {
            logger.info("Automated Claim Issue for citizen claim ref no: {}", claim.getReferenceNumber());
            String submitterName = claim.getClaimData().getClaimant().getName();
            this.postClaimOrchestrationHandler
                .citizenIssueHandler(new CitizenClaimCreatedEvent(claim, submitterName, authorisation));
        }
    }

}
