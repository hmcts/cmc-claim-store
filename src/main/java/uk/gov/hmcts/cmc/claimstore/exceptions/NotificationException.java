package uk.gov.hmcts.cmc.claimstore.exceptions;

public class NotificationException extends RuntimeException {

    public NotificationException(final Exception cause) {
        super(cause);
    }
}
