package uk.gov.hmcts.cmc.scheduler.exceptions;

import uk.gov.hmcts.reform.logging.exception.AbstractLoggingException;

import static uk.gov.hmcts.reform.logging.exception.AlertLevel.P1;
import static uk.gov.hmcts.reform.logging.exception.ErrorCode.UNKNOWN;

public class JobNotFoundException extends AbstractLoggingException {

    public JobNotFoundException(Throwable cause) {
        super(P1, UNKNOWN, cause);
    }

    public JobNotFoundException(String message) {
        super(P1, UNKNOWN, message);
    }

    public JobNotFoundException(String message, Throwable cause) {
        super(P1, UNKNOWN, message, cause);
    }
}
