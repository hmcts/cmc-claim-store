package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.CountyCourtJudgmentAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.DefendantLinkingException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ResponseAlreadySubmittedException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;

@Service
public class DefendantResponseService {

    private final EventProducer eventProducer;
    private final ClaimService claimService;
    private final UserService userService;

    public DefendantResponseService(
        EventProducer eventProducer,
        ClaimService claimService,
        UserService userService
    ) {
        this.eventProducer = eventProducer;
        this.claimService = claimService;
        this.userService = userService;
    }

    @Transactional
    public Claim save(
        String externalId,
        String defendantId,
        Response response,
        String authorization
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorization);

        if (!isClaimLinkedWithDefendant(claim, defendantId)) {
            throw new DefendantLinkingException(
                String.format("Claim %s is not linked with defendant %s", claim.getReferenceNumber(), defendantId)
            );
        }

        if (isResponseAlreadySubmitted(claim)) {
            throw new ResponseAlreadySubmittedException(claim.getId());
        }

        if (isCCJAlreadyRequested(claim)) {
            throw new CountyCourtJudgmentAlreadyRequestedException(claim.getId());
        }

        String defendantEmail = userService.getUserDetails(authorization).getEmail();
        claimService.saveDefendantResponse(claim, defendantEmail, response, authorization);

        Claim claimAfterSavingResponse = claimService.getClaimByExternalId(externalId, authorization);

        eventProducer.createDefendantResponseEvent(claimAfterSavingResponse);

        return claimAfterSavingResponse;
    }

    private boolean isClaimLinkedWithDefendant(Claim claim, String defendantId) {
        return claim.getDefendantId() != null && claim.getDefendantId().equals(defendantId);
    }

    private boolean isResponseAlreadySubmitted(Claim claim) {
        return claim.getRespondedAt() != null;
    }

    private boolean isCCJAlreadyRequested(Claim claim) {
        return claim.getCountyCourtJudgmentRequestedAt() != null;
    }
}
