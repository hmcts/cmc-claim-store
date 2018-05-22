package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.NotNull;

@Service
public class CountyCourtJudgmentRule {

    public void assertCountyCourtJudgementCanBeRequested(@NotNull Claim claim) {
        Objects.requireNonNull(claim, "claim object can not be null");
        if (isResponseAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("Response for the claim " + claim.getExternalId() + " was submitted");
        }

        if (isCountyCourtJudgmentAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("County Court Judgment for the claim "
                + claim.getExternalId() + " was submitted");
        }

        if (!canCountyCourtJudgmentBeRequestedYet(LocalDate.now(), claim)) {
            throw new ForbiddenActionException(
                "County Court Judgment for claim " + claim.getExternalId() + " cannot be requested yet"
            );
        }
    }

    protected boolean canCountyCourtJudgmentBeRequestedYet(LocalDate currentDate, Claim claim) {
        return currentDate.isAfter(claim.getResponseDeadline());
    }

    private boolean isResponseAlreadySubmitted(Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isCountyCourtJudgmentAlreadySubmitted(Claim claim) {
        return claim.getCountyCourtJudgment() != null;
    }
}
