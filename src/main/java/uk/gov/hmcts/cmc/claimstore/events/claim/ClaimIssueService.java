package uk.gov.hmcts.cmc.claimstore.events.claim;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimIssueService {

    private final Logger logger = LoggerFactory.getLogger(ClaimIssueService.class);

    private final UserService userService;
    private final CaseMapper caseMapper;
    private final CCDCaseApi ccdCaseApi;
    private final PostClaimOrchestrationHandler postClaimOrchestrationHandler;

    @LogExecutionTime
    public void issueCreatedClaims() {
        logger.info("==== Started Automated Claim Issue ====");

        final User user = userService.authenticateAnonymousCaseWorker();
        final String authorisation = user.getAuthorisation();
        List<CCDCase> casesAtCreatedState = ccdCaseApi.getCCDCaseByState(ClaimState.CREATE, user);
        List<CCDCase> stuckCases = casesAtCreatedState.stream()
            .filter(ccdCase -> (ccdCase.getPreviousServiceCaseReference() != null))
            .collect(Collectors.toList());

        logger.info("Automated Claim Issue - Total claims in Create state (Stuck Claims): {}", stuckCases.size());

        stuckCases
            .stream()
            .forEach(ccdCase -> processClaimIssue(ccdCase, authorisation));

        logger.info("==== Finished Automated Claim Issue ====");
    }

    private void processClaimIssue(CCDCase ccdCase, String authorisation) {
        try {
            logger.info("Processing automated Claim Issue for ref no: {}", ccdCase.getPreviousServiceCaseReference());
            Claim claim = caseMapper.from(ccdCase);
            triggerAsyncOperation(claim, authorisation);
        } catch (Exception exception) {
            logger.error("Failed to process claim issue for reference number: "
                + ccdCase.getPreviousServiceCaseReference(), exception);
        }
    }

    private void triggerAsyncOperation(Claim claim, String authorisation) {
        if (claim.getClaimData().isClaimantRepresented()) {
            logger.info("Automated claim issue should not been triggered "
                + "as this is Legal Rep case which is deprecated. {}", claim.getReferenceNumber());
        } else {
            logger.info("Automated claim issue for citizen claim ref no: {}", claim.getReferenceNumber());
            String submitterName = claim.getClaimData().getClaimant().getName();
            this.postClaimOrchestrationHandler
                .citizenIssueHandler(new CitizenClaimCreatedEvent(claim, submitterName, authorisation));
        }
    }

}
