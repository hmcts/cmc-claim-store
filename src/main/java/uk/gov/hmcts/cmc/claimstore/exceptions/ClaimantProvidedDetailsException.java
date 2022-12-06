package uk.gov.hmcts.cmc.claimstore.exceptions;

public class ClaimantProvidedDetailsException extends RuntimeException {
    public ClaimantProvidedDetailsException(String message, Throwable cause) {
        super(message, cause);
    }
}
