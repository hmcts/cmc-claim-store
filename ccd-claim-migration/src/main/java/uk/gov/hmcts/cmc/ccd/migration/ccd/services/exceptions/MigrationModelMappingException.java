package uk.gov.hmcts.cmc.ccd.migration.ccd.services.exceptions;

public class MigrationModelMappingException extends RuntimeException {

    public MigrationModelMappingException(String msg, Exception ex) {
        super(msg, ex);
    }
}
