package uk.gov.hmcts.cmc.claimstore.exceptions;

public class TemplateException extends RuntimeException {

    public TemplateException(Throwable cause) {
        super("An error occurred during template processing", cause);
    }

}
