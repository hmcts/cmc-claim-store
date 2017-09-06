package uk.gov.hmcts.cmc.claimstore.exceptions;

public class MoreTimeRequestedAfterDeadlineException extends ConflictException {
    public MoreTimeRequestedAfterDeadlineException() {
        super("more time requested after deadline");
    }

    public MoreTimeRequestedAfterDeadlineException(final String message) {
        super(message);
    }
}
