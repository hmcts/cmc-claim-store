package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.CountyCourtJudgmentAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ResponseAlreadySubmittedException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ResponseData;

@Service
public class DefendantResponseService {

    private final EventProducer eventProducer;
    private final ClaimService claimService;
    private final UserService userService;
    private final AuthorisationService authorisationService;

    public DefendantResponseService(
        final EventProducer eventProducer,
        final ClaimService claimService,
        final UserService userService,
        final AuthorisationService authorisationService) {
        this.eventProducer = eventProducer;
        this.claimService = claimService;
        this.userService = userService;
        this.authorisationService = authorisationService;
    }

    @Transactional
    public Claim save(
        final long claimId,
        final String defendantId,
        final ResponseData responseData,
        final String authorization
    ) {
        final Claim claim = claimService.getClaimById(claimId);

        authorisationService.assertIsDefendantOnClaim(claim, defendantId);

        if (isResponseAlreadySubmitted(claim)) {
            throw new ResponseAlreadySubmittedException(claimId);
        }

        if (isCCJAlreadyRequested(claim)) {
            throw new CountyCourtJudgmentAlreadyRequestedException(claimId);
        }

        final String defendantEmail = userService.getUserDetails(authorization).getEmail();
        claimService.saveDefendantResponse(claimId, defendantId, defendantEmail, responseData);

        final Claim claimAfterSavingResponse = claimService.getClaimById(claimId);

        eventProducer.createDefendantResponseEvent(claimAfterSavingResponse);

        return claimAfterSavingResponse;
    }

    private boolean isResponseAlreadySubmitted(final Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isCCJAlreadyRequested(final Claim claim) {
        return claim.getCountyCourtJudgmentRequestedAt() != null;
    }
}
