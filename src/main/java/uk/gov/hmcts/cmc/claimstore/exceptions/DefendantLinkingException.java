package uk.gov.hmcts.cmc.claimstore.exceptions;

public class DefendantLinkingException extends RuntimeException {
    public DefendantLinkingException(String message) {
        super(message);
    }
}
