package uk.gov.hmcts.cmc.claimstore.exceptions;

public class DocumentManagementException extends Exception {
    public DocumentManagementException(String message) {
        super(message);
    }

    public DocumentManagementException(String message, Throwable t) {
        super(message, t);
    }
}
