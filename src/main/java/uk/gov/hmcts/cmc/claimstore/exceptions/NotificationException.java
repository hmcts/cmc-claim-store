package uk.gov.hmcts.cmc.claimstore.exceptions;

public class NotificationException extends RuntimeException {

    public NotificationException(final String message) {
        super(message);
    }

    public NotificationException(final Exception cause) {
        super(cause);
    }
}
