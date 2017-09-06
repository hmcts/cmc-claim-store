package uk.gov.hmcts.cmc.claimstore.services.staff.models;

public class EmailContent {

    private final String subject;
    private final String body;

    public EmailContent(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

}
