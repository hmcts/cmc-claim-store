package uk.gov.hmcts.cmc.claimstore.exceptions;

public class SocketTimeoutException  extends RuntimeException {
    public SocketTimeoutException(String message) {
        super(message);
    }
}
