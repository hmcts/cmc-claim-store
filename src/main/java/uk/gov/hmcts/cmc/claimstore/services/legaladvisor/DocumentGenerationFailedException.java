package uk.gov.hmcts.cmc.claimstore.services.legaladvisor;

public class DocumentGenerationFailedException  extends RuntimeException {

    public DocumentGenerationFailedException(Throwable cause) {
        super(cause);
    }
}
