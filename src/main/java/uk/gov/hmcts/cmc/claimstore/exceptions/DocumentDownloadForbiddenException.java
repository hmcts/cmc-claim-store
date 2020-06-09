package uk.gov.hmcts.cmc.claimstore.exceptions;

public final class DocumentDownloadForbiddenException extends ForbiddenActionException {
    public DocumentDownloadForbiddenException(String message) {
        super(message);
    }
}
