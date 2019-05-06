package uk.gov.hmcts.cmc.claimstore.exceptions;

public class MediationCSVGenerationException extends RuntimeException {
    public MediationCSVGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MediationCSVGenerationException(String message) {
        super(message);
    }
}
