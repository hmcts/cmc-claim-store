package uk.gov.hmcts.cmc.claimstore.exceptions;

public class CoreCaseDataStoreException extends RuntimeException {
    public CoreCaseDataStoreException(String message) {
        super(message);
    }

    public CoreCaseDataStoreException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
