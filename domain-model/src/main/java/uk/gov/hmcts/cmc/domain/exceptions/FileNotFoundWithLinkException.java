package uk.gov.hmcts.cmc.domain.exceptions;

public class FileNotFoundWithLinkException extends IllegalArgumentException {

    public FileNotFoundWithLinkException(String message, Exception ex) {
        super(message, ex);
    }

}
