package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;

@Service
public class CountyCourtJudgmentRule {

    public void assertCanIssueCountyCourtJudgment(final Claim claim) {

        if (isResponseAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("Response for the claim " + claim.getExternalId() + " was submitted");
        }

        if (isCountyCourtJudgmentAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("County Court Judgment for the claim "
                + claim.getExternalId() + " was submitted");
        }

        if (!canCountyCourtJudgmentBeRequestedYet(claim)) {
            throw new ForbiddenActionException(
                "County Court Judgment for claim " + claim.getExternalId() + " cannot be requested yet"
            );
        }
    }

    private boolean canCountyCourtJudgmentBeRequestedYet(final Claim claim) {
        return LocalDate.now().isAfter(claim.getResponseDeadline());
    }

    private boolean isResponseAlreadySubmitted(final Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isCountyCourtJudgmentAlreadySubmitted(final Claim claim) {
        return claim.getCountyCourtJudgment() != null;
    }
}
