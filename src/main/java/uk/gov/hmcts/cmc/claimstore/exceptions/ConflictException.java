package uk.gov.hmcts.cmc.claimstore.exceptions;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
