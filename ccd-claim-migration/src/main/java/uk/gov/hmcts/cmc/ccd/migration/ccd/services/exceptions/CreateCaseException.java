package uk.gov.hmcts.cmc.ccd.migration.ccd.services.exceptions;

public class CreateCaseException extends RuntimeException {

    public CreateCaseException(String msg, Exception ex) {
        super(msg, ex);
    }
}
