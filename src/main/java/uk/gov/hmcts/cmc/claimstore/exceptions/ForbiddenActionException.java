package uk.gov.hmcts.cmc.claimstore.exceptions;

public class ForbiddenActionException extends RuntimeException {
    public ForbiddenActionException(String message) {
        super(message);
    }
}
