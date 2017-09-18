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
public class CountyCourtJudgmentService {

    private final ClaimRepository claimRepository;
    private final JsonMapper jsonMapper;
    private final EventProducer eventProducer;

    @Autowired
    public CountyCourtJudgmentService(
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
            throw new ForbiddenActionException("Claim " + claimId + "does not belog to user" + submitterId);
        }

        if (isResponseAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("Response for the claim " + claimId + " was submitted");
        }

        if (isCountyCourtJudgmentAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("County Court Judgment for the claim " + claimId + " was submitted");
        }

        if (!canCountyCourtJudgmentBeRequestedYet(claim)) {
            throw new ForbiddenActionException(
                "County Court Judgment for claim " + claimId + " cannot be requested yet"
            );
        }

        claimRepository.saveCountyCourtJudgment(claimId, jsonMapper.toJson(data));

        Claim claimWithCCJ = getClaim(claimId);

        eventProducer.createCountyCourtJudgmentSubmittedEvent(claimWithCCJ);

        return claimWithCCJ;
    }

    private boolean canCountyCourtJudgmentBeRequestedYet(final Claim claim) {
        return LocalDate.now().isAfter(claim.getResponseDeadline());
    }

    private boolean isResponseAlreadySubmitted(final Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isClaimSubmittedByUser(final Claim claim, final long submitterId) {
        return claim.getSubmitterId().equals(submitterId);
    }

    private boolean isCountyCourtJudgmentAlreadySubmitted(final Claim claim) {
        return claim.getCountyCourtJudgment() != null;
    }

    private Claim getClaim(final long claimId) {
        return claimRepository.getById(claimId)
            .orElseThrow(() -> new NotFoundException("Claim not found by id: " + claimId));
    }
}
