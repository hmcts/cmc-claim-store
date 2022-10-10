package uk.gov.hmcts.cmc.claimstore.exceptions;

public class UnprocessableEntityException extends RuntimeException {
    public UnprocessableEntityException(String message) {
        super(message);
    }

    public UnprocessableEntityException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
