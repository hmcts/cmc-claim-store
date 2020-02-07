package uk.gov.hmcts.cmc.claimstore.controllers.advices;

import lombok.ToString;

@ToString
public class ExceptionForClient {

    private final int status;
    private final String message;

    public ExceptionForClient(int httpStatus, String message) {
        this.status = httpStatus;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
