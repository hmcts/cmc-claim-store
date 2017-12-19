package uk.gov.hmcts.cmc.domain.exceptions;

public class NotificationException extends RuntimeException {

    public NotificationException(String message) {
        super(message);
    }

    public NotificationException(Exception cause) {
        super(cause);
    }
}
