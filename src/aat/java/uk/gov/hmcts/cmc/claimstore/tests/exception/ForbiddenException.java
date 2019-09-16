package uk.gov.hmcts.cmc.claimstore.tests.exception;

public class ForbiddenException extends IllegalArgumentException {

    public ForbiddenException(String message, Exception ex) {
        super(message, ex);
    }

    public ForbiddenException(String message) {
        super(message);
    }

}
