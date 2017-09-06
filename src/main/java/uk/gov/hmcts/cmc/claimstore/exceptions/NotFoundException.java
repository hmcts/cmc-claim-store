package uk.gov.hmcts.cmc.claimstore.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("not found");
    }

    public NotFoundException(String message) {
        super(message);
    }
}
