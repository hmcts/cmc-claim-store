package uk.gov.hmcts.cmc.email;

public class EmailSendFailedException extends RuntimeException {

    public EmailSendFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailSendFailedException(Throwable cause) {
        super(cause);
    }
}
