package uk.gov.hmcts.cmc.claimstore.exceptions;

public class DocumentManagementException extends Throwable {
    public DocumentManagementException(final String message, final Exception e) {
        super(message, e);
    }
}
