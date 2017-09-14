package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefaultJudgment;
import uk.gov.hmcts.cmc.claimstore.repositories.DefaultJudgmentRepository;

import java.time.LocalDate;

@Component
public class DefaultJudgmentService {

    private final DefaultJudgmentRepository defaultJudgmentRepository;
    private final ClaimService claimService;
    private final EventProducer eventProducer;

    @Autowired
    public DefaultJudgmentService(
        ClaimService claimService,
        DefaultJudgmentRepository defaultJudgmentRepository,
        EventProducer eventProducer) {
        this.defaultJudgmentRepository = defaultJudgmentRepository;
        this.claimService = claimService;
        this.eventProducer = eventProducer;
    }

    public DefaultJudgment getByClaimId(final long claimId) {
        return defaultJudgmentRepository
            .getByClaimId(claimId)
            .orElseThrow(() -> new NotFoundException("Default judgment for claim " + claimId + " not found"));
    }

    @Transactional
    public DefaultJudgment save(final long submitterId, final String data, final long claimId) {

        Claim claim = claimService.getClaimById(claimId);

        if (!isClaimSubmittedByUser(claim, submitterId)) {
            throw new ForbiddenActionException("It's not your claim");
        }

        if (isResponseAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("Response for the claim was submitted");
        }

        if (isDefaultJudgmentAlreadySubmitted(claimId)) {
            throw new ForbiddenActionException("Default Judgment for the claim was submitted");
        }

        if (!canDefaultJudgmentBeRequestedYet(claim.getResponseDeadline())) {
            throw new ForbiddenActionException("You must not request for default judgment yet");
        }

        final Long defaultJudgmentId = defaultJudgmentRepository.save(
            claimId, claim.getSubmitterId(), claim.getClaimData().getExternalId().toString(), data
        );

        DefaultJudgment defaultJudgment = defaultJudgmentRepository.getById(defaultJudgmentId).get();

        eventProducer.createDefaultJudgmentSubmittedEvent(defaultJudgment, claim);

        return defaultJudgment;
    }

    private boolean canDefaultJudgmentBeRequestedYet(final LocalDate responseDeadline) {
        return LocalDate.now().isAfter(responseDeadline);
    }

    private boolean isResponseAlreadySubmitted(final Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isClaimSubmittedByUser(final Claim claim, final long submitterId) {
        return claim.getSubmitterId().equals(submitterId);
    }

    private boolean isDefaultJudgmentAlreadySubmitted(final long claimId) {
        return defaultJudgmentRepository.getByClaimId(claimId).isPresent();
    }
}
