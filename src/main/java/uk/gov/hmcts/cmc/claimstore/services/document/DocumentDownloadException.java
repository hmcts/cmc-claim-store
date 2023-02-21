package uk.gov.hmcts.cmc.claimstore.services.document;

public class DocumentDownloadException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Unable to download document %s from document management.";

    public DocumentDownloadException(String fileName, Throwable t) {
        super(String.format(MESSAGE_TEMPLATE, fileName), t);
    }
}
