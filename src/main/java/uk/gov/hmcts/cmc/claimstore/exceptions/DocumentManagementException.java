package uk.gov.hmcts.cmc.claimstore.exceptions;

public class DocumentManagementException extends RuntimeException {
    public DocumentManagementException(String message) {
        super(message);
    }
}
