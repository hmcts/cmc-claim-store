package uk.gov.hmcts.cmc.claimstore.exceptions;

public class ClaimantResponseAlreadySubmittedException extends ConflictException {
    public ClaimantResponseAlreadySubmittedException(long claimId) {
        super("Claimant response for the claim " + claimId + " was already submitted");
    }
}
