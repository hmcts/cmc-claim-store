package uk.gov.hmcts.cmc.claimstore.exceptions;

public class CoreCaseDataStoreException extends RuntimeException {

    public CoreCaseDataStoreException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
