package uk.gov.hmcts.cmc.scheduler.exceptions;

public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(String message) {
        super(message);
    }
}
