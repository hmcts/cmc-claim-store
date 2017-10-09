package uk.gov.hmcts.cmc.claimstore.exceptions;

public class ResponseAlreadySubmittedException extends ForbiddenActionException {
    public ResponseAlreadySubmittedException(long claimId) {
        super("Response for the claim " + claimId + " was already submitted");
    }
}
