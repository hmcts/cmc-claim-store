package uk.gov.hmcts.cmc.email;

public class EmailSendFailedException extends RuntimeException {

    public EmailSendFailedException(Throwable cause) {
        super(cause);
    }
}
