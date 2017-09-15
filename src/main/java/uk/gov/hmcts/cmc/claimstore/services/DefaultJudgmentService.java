package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.ClaimRepository;

import java.time.LocalDate;
import java.util.Map;

@Component
public class DefaultJudgmentService {

    private final ClaimRepository claimRepository;
    private final JsonMapper jsonMapper;
    private final EventProducer eventProducer;

    @Autowired
    public DefaultJudgmentService(
        ClaimRepository claimRepository,
        JsonMapper jsonMapper,
        EventProducer eventProducer) {
        this.claimRepository = claimRepository;
        this.jsonMapper = jsonMapper;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public Claim save(final long submitterId, final Map<String, Object> data, final long claimId) {

        Claim claim = getClaim(claimId);

        if (!isClaimSubmittedByUser(claim, submitterId)) {
            throw new ForbiddenActionException("It's not your claim");
        }

        if (isResponseAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("Response for the claim was submitted");
        }

        if (isDefaultJudgmentAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("Default Judgment for the claim was submitted");
        }

        if (!canDefaultJudgmentBeRequestedYet(claim)) {
            throw new ForbiddenActionException("You must not request for default judgment yet");
        }

        claimRepository.saveDefaultJudgment(claimId, jsonMapper.toJson(data));

        Claim claimWithDefaultJudgment = getClaim(claimId);

        eventProducer.createDefaultJudgmentSubmittedEvent(claimWithDefaultJudgment);

        return claimWithDefaultJudgment;
    }

    private boolean canDefaultJudgmentBeRequestedYet(final Claim claim) {
        return LocalDate.now().isAfter(claim.getResponseDeadline());
    }

    private boolean isResponseAlreadySubmitted(final Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isClaimSubmittedByUser(final Claim claim, final long submitterId) {
        return claim.getSubmitterId().equals(submitterId);
    }

    private boolean isDefaultJudgmentAlreadySubmitted(final Claim claim) {
        return claim.getDefaultJudgment() != null;
    }

    private Claim getClaim(final long claimId) {
        return claimRepository.getById(claimId)
            .orElseThrow(() -> new NotFoundException("Claim not found by id: " + claimId));
    }
}
