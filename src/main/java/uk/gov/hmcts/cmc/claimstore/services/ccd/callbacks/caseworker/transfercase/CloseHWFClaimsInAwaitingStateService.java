package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.AWAITING_RESPONSE_HWF;

@Service
public class CloseHWFClaimsInAwaitingStateService {

    private final Logger logger = LoggerFactory.getLogger(CloseHWFClaimsInAwaitingStateService.class);

    public static final int MAX_DAYS_ALLOWED_IN_WAITING_STATE = 95;

    private final CaseMapper caseMapper;
    private final UserService userService;
    private final ClaimService claimService;
    private final CoreCaseDataService coreCaseDataService;

    @Autowired
    public CloseHWFClaimsInAwaitingStateService(
        CaseMapper caseMapper,
        UserService userService,
        ClaimService claimService,
        CoreCaseDataService coreCaseDataService
    ) {
        this.userService = userService;
        this.caseMapper = caseMapper;
        this.claimService = claimService;
        this.coreCaseDataService = coreCaseDataService;
    }

    public void findCasesAndClose() {
        User user = userService.authenticateAnonymousCaseWorker();
        String authorisation = user.getAuthorisation();
        List<Claim> claimsAwaitingResponse = claimService.getClaimsByState(AWAITING_RESPONSE_HWF, user);
        logger.info("Claims in AWAITING_RESPONSE_HWF state: {}", claimsAwaitingResponse.size());
        claimsAwaitingResponse.stream().forEach(claim -> closeClaimsInAwaitingHWFState(claim, authorisation));
    }

    private void closeClaimsInAwaitingHWFState(Claim claim, String authorisation) {
        if (DAYS.between(claim.getLastModified(), now()) > MAX_DAYS_ALLOWED_IN_WAITING_STATE) {
            coreCaseDataService.update(authorisation, caseMapper.to(claim), CaseEvent.CLOSE_AWAITING_RESPONSE_HWF);
            logger.info("Awaiting HWF Claim Closed - {}", claim.getReferenceNumber());
        }
    }

}
