package uk.gov.hmcts.cmc.claimstore.exceptions;

public class DuplicateKeyException extends RuntimeException {
    public DuplicateKeyException(Exception cause) {
        super(cause);
    }

    public DuplicateKeyException(String message) {
        super(message);
    }
}
