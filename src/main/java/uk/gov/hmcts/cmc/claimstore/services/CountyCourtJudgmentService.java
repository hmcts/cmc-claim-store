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
        ClaimService claimService,
        AuthorisationService authorisationService,
        EventProducer eventProducer
    ) {
        this.claimService = claimService;
        this.authorisationService = authorisationService;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public Claim save(String submitterId, CountyCourtJudgment countyCourtJudgment, long claimId) {

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

    private boolean canCountyCourtJudgmentBeRequestedYet(Claim claim) {
        return LocalDate.now().isAfter(claim.getResponseDeadline());
    }

    private boolean isResponseAlreadySubmitted(Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isCountyCourtJudgmentAlreadySubmitted(Claim claim) {
        return claim.getCountyCourtJudgment() != null;
    }
}
