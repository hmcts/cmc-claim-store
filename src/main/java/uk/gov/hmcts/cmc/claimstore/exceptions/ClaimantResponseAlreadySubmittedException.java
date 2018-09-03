package uk.gov.hmcts.cmc.claimstore.exceptions;

public class ClaimantResponseAlreadySubmittedException extends ConflictException {
    public ClaimantResponseAlreadySubmittedException(String externalId) {
        super("Claimant response for the claim " + externalId + " was already submitted");
    }
}
