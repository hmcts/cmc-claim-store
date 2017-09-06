package uk.gov.hmcts.cmc.email;

import java.util.Collections;
import java.util.List;

public class EmailData {

    private final String to;

    private final String subject;

    private final String message;

    private final List<EmailAttachment> attachments;

    public EmailData(final String to,
                     final String subject,
                     final String message,
                     final List<EmailAttachment> attachments) {

        this.to = to;
        this.subject = subject;
        this.message = message;
        this.attachments = Collections.unmodifiableList(attachments);
    }

    public boolean hasAttachments() {
        return this.attachments != null && !this.attachments.isEmpty();
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public List<EmailAttachment> getAttachments() {
        return this.attachments;
    }

    @Override
    public String toString() {
        return String.format("EmailData{to='%s', subject='%s', message='%s', attachments=%s}",
            to, subject, message, attachments);
    }
}
