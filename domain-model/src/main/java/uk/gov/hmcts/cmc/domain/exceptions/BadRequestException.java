package uk.gov.hmcts.cmc.domain.exceptions;

public class BadRequestException extends IllegalArgumentException {

    public BadRequestException(String message) {
        super(message);
    }

}
