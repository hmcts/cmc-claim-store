package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.CountyCourtJudgmentAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ResponseAlreadySubmittedException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Response;

@Service
public class DefendantResponseService {

    private final EventProducer eventProducer;
    private final ClaimService claimService;
    private final UserService userService;
    private final AuthorisationService authorisationService;

    public DefendantResponseService(
        EventProducer eventProducer,
        ClaimService claimService,
        UserService userService,
        AuthorisationService authorisationService) {
        this.eventProducer = eventProducer;
        this.claimService = claimService;
        this.userService = userService;
        this.authorisationService = authorisationService;
    }

    @Transactional
    public Claim save(
        String externalId,
        String defendantId,
        Response response,
        String authorization
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorization);

        if (isResponseAlreadySubmitted(claim)) {
            throw new ResponseAlreadySubmittedException(claim.getId());
        }

        if (isCCJAlreadyRequested(claim)) {
            throw new CountyCourtJudgmentAlreadyRequestedException(claim.getId());
        }

        String defendantEmail = userService.getUserDetails(authorization).getEmail();
        claimService.saveDefendantResponse(claim.getId(), defendantId, defendantEmail, response);

        Claim claimAfterSavingResponse = claimService.getClaimByExternalId(externalId, authorization);

        eventProducer.createDefendantResponseEvent(claimAfterSavingResponse);

        return claimAfterSavingResponse;
    }

    private boolean isResponseAlreadySubmitted(Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isCCJAlreadyRequested(Claim claim) {
        return claim.getCountyCourtJudgmentRequestedAt() != null;
    }
}
