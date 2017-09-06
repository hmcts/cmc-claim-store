package uk.gov.hmcts.cmc.claimstore.exceptions;

public class ForbiddenActionException extends RuntimeException {
    public ForbiddenActionException() {
        super("You are not allowed");
    }

    public ForbiddenActionException(String message) {
        super(message);
    }
}
