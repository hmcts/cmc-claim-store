package uk.gov.hmcts.cmc.scheduler.exceptions;

public class JobException extends RuntimeException {

    public JobException(String message, Throwable cause) {
        super(message, cause);
    }
}
