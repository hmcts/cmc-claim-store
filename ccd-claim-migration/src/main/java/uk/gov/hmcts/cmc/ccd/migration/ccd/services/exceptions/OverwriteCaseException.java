package uk.gov.hmcts.cmc.ccd.migration.ccd.services.exceptions;

public class OverwriteCaseException extends RuntimeException {

    public OverwriteCaseException(String msg, Exception ex) {
        super(msg, ex);
    }
}
