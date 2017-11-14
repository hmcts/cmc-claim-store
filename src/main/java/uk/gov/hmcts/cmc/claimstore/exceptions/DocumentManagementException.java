package uk.gov.hmcts.cmc.claimstore.exceptions;

public class DocumentManagementException extends RuntimeException {
    public DocumentManagementException(final String message) {
        super(message);
    }
}
