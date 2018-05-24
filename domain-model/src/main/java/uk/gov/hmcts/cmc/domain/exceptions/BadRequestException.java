package uk.gov.hmcts.cmc.domain.exceptions;

public class BadRequestException extends IllegalArgumentException {

    public BadRequestException(String message, Exception ex) {
        super(message, ex);
    }

    public BadRequestException(String message) {
        super(message);
    }

}
