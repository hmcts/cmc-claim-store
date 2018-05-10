package uk.gov.hmcts.cmc.scheduler.exceptions;

import uk.gov.hmcts.reform.logging.exception.AbstractLoggingException;

import static uk.gov.hmcts.reform.logging.exception.AlertLevel.P1;
import static uk.gov.hmcts.reform.logging.exception.ErrorCode.UNKNOWN;

public class JobException extends AbstractLoggingException {

    public JobException(Throwable cause) {
        super(P1, UNKNOWN, cause);
    }

    public JobException(String message) {
        super(P1, UNKNOWN, message);
    }

    public JobException(String message, Throwable cause) {
        super(P1, UNKNOWN, message, cause);
    }
}
