package uk.gov.hmcts.cmc.claimstore.exceptions;

public class OnHoldClaimAccessAttemptException extends RuntimeException {
    public OnHoldClaimAccessAttemptException(String message) {
        super(message);
    }
}
