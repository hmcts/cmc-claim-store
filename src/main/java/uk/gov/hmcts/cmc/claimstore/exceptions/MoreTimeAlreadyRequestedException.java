package uk.gov.hmcts.cmc.claimstore.exceptions;

public class MoreTimeAlreadyRequestedException extends ConflictException {
    public MoreTimeAlreadyRequestedException(String message) {
        super(message);
    }
}
