package uk.gov.hmcts.cmc.claimstore.exceptions;

public class MoreTimeRequestedAfterDeadlineException extends ConflictException {
    public MoreTimeRequestedAfterDeadlineException(String message) {
        super(message);
    }
}
