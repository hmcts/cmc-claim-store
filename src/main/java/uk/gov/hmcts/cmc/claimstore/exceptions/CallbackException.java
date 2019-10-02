package uk.gov.hmcts.cmc.claimstore.exceptions;

public class CallbackException extends RuntimeException {
    public CallbackException(String message) {
        super(message);
    }

    public CallbackException(String message, Throwable t) {
        super(message, t);
    }
}
