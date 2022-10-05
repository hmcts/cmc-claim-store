package uk.gov.hmcts.cmc.claimstore.exceptions;

public class DuplicateKeyException extends RuntimeException {
    public DuplicateKeyException(Exception cause) {
        super(cause);
    }
}
