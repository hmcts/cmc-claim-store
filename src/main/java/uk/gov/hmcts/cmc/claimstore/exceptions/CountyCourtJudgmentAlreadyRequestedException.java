package uk.gov.hmcts.cmc.claimstore.exceptions;

public class CountyCourtJudgmentAlreadyRequestedException extends ForbiddenActionException {
    public CountyCourtJudgmentAlreadyRequestedException(long claimId) {
        super("County Court Judgment for the claim " + claimId + " was already requested");
    }
}
