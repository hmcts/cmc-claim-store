package uk.gov.hmcts.cmc.claimstore.exceptions;

public class ConflictException extends RuntimeException {
    public ConflictException(final String message) {
        super(message);
    }
}
