package uk.gov.hmcts.cmc.claimstore.exceptions;

public class InvalidApplicationException extends RuntimeException {
    public InvalidApplicationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
