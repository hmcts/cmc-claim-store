package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.CountyCourtJudgmentAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ResponseAlreadySubmittedException;
import uk.gov.hmcts.cmccase.models.Claim;
import uk.gov.hmcts.cmccase.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;

@Service
public class DefendantResponseService {

    private final ClaimRepository claimRepository;
    private final JsonMapper jsonMapper;
    private final EventProducer eventProducer;
    private final ClaimService claimService;
    private final UserService userService;

    public DefendantResponseService(
        final ClaimRepository claimRepository,
        final JsonMapper jsonMapper,
        final EventProducer eventProducer,
        final ClaimService claimService,
        final UserService userService) {
        this.claimRepository = claimRepository;
        this.jsonMapper = jsonMapper;
        this.eventProducer = eventProducer;
        this.claimService = claimService;
        this.userService = userService;
    }

    @Transactional
    public Claim save(
        final long claimId,
        final String defendantId,
        final ResponseData responseData,
        final String authorization
    ) {
        final Claim claim = claimService.getClaimById(claimId);

        if (isResponseAlreadySubmitted(claim)) {
            throw new ResponseAlreadySubmittedException(claimId);
        }

        if (isCCJAlreadyRequested(claim)) {
            throw new CountyCourtJudgmentAlreadyRequestedException(claimId);
        }

        final String defendantEmail = userService.getUserDetails(authorization).getEmail();
        claimRepository.saveDefendantResponse(
            claimId, defendantId, defendantEmail, jsonMapper.toJson(responseData)
        );

        final Claim claimAfterSavingResponse = claimService.getClaimById(claimId);

        eventProducer.createDefendantResponseEvent(claimAfterSavingResponse);

        return claimAfterSavingResponse;
    }

    private boolean isResponseAlreadySubmitted(final Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isCCJAlreadyRequested(final Claim claim) {
        return null != claim.getCountyCourtJudgmentRequestedAt();
    }
}
