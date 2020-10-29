package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
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
        logger.info("Automated Claim Issue - Total claims in Create state: {}", createdClaims.size());
        createdClaims.stream().forEach(claim -> issueClaimsPendingInCreatedState(claim, authorisation));
    }

    private void issueClaimsPendingInCreatedState(Claim claim, String authorisation) {
        logger.info("Automated Claim Issue - {}", claim.getReferenceNumber());
        if (claim.getClaimData().isClaimantRepresented()) {
            String submitterName = claim.getClaimData().getClaimants().stream()
                .findFirst()
                .map(Party::getRepresentative)
                .map(Optional::get)
                .map(Representative::getOrganisationName)
                .orElseThrow(() -> new IllegalArgumentException(MISSING_REPRESENTATIVE));

            this.postClaimOrchestrationHandler
                .representativeIssueHandler(new RepresentedClaimCreatedEvent(claim, submitterName, authorisation));
        } else {
            String submitterName = claim.getClaimData().getClaimant().getName();
            this.postClaimOrchestrationHandler
                .citizenIssueHandler(new CitizenClaimCreatedEvent(claim, submitterName, authorisation));
        }
    }

}
