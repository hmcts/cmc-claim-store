package uk.gov.hmcts.cmc.claimstore.exceptions;

public class ClaimCreationDisabledException extends RuntimeException {
    public ClaimCreationDisabledException(String message) {
        super(message);
    }
}
