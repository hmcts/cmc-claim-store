package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.DefendantResponseRepository;

import java.util.List;

@Service
public class DefendantResponseService {

    private final DefendantResponseRepository defendantResponseRepository;
    private final JsonMapper jsonMapper;
    private final EventProducer eventProducer;
    private final ClaimService claimService;
    private final UserService userService;

    public DefendantResponseService(
        final DefendantResponseRepository defendantResponseRepository,
        final JsonMapper jsonMapper,
        final EventProducer eventProducer,
        final ClaimService claimService,
        final UserService userService) {
        this.defendantResponseRepository = defendantResponseRepository;
        this.jsonMapper = jsonMapper;
        this.eventProducer = eventProducer;
        this.claimService = claimService;
        this.userService = userService;
    }

    public DefendantResponse getById(final long responseId) {
        return defendantResponseRepository
            .getById(responseId)
            .orElseThrow(() ->
                new NotFoundException("Defendant response expected to have been saved, but wasn't"));
    }

    public List<DefendantResponse> getByDefendantId(final long defendantId) {
        return defendantResponseRepository.getByDefendantId(defendantId);
    }

    @Transactional
    public DefendantResponse save(
        final long claimId,
        final long defendantId,
        final ResponseData responseData,
        final String authorization
    ) {
        final String defendantEmail = userService.getUserDetails(authorization).getEmail();
        final Long responseId = defendantResponseRepository.save(claimId, defendantId, defendantEmail,
            jsonMapper.toJson(responseData));
        final Claim claim = claimService.getClaimById(claimId);
        final DefendantResponse response = getById(responseId);

        eventProducer.createDefendantResponseEvent(claim, response);

        return response;
    }

    public DefendantResponse getByClaimId(long claimId) {
        return defendantResponseRepository.getByClaimId(claimId)
            .orElseThrow(() ->
                new NotFoundException("Couldn't find defendant response for claim ID: " + claimId)
            );
    }

}
