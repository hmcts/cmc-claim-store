package uk.gov.hmcts.cmc.claimstore.exceptions;

public class UnprocessableEntityException extends RuntimeException {
    public UnprocessableEntityException(final String message) {
        super(message);
    }
}
