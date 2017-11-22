package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;

import java.time.LocalDate;

@Component
public class CountyCourtJudgmentService {

    private final ClaimService claimService;
    private final AuthorisationService authorisationService;
    private final EventProducer eventProducer;

    @Autowired
    public CountyCourtJudgmentService(
        final ClaimService claimService,
        final AuthorisationService authorisationService,
        final EventProducer eventProducer
    ) {
        this.claimService = claimService;
        this.authorisationService = authorisationService;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public Claim save(final String submitterId, final CountyCourtJudgment countyCourtJudgment, final long claimId) {

        Claim claim = claimService.getClaimById(claimId);

        authorisationService.assertIsSubmitterOnClaim(claim, submitterId);

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

        claimService.saveCountyCourtJudgment(claimId, countyCourtJudgment);

        Claim claimWithCCJ = claimService.getClaimById(claimId);

        eventProducer.createCountyCourtJudgmentRequestedEvent(claimWithCCJ);

        return claimWithCCJ;
    }

    private boolean canCountyCourtJudgmentBeRequestedYet(final Claim claim) {
        return LocalDate.now().isAfter(claim.getResponseDeadline());
    }

    private boolean isResponseAlreadySubmitted(final Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isClaimSubmittedByUser(final Claim claim, final String submitterId) {
        return claim.getSubmitterId().equals(submitterId);
    }

    private boolean isCountyCourtJudgmentAlreadySubmitted(final Claim claim) {
        return claim.getCountyCourtJudgment() != null;
    }
}
