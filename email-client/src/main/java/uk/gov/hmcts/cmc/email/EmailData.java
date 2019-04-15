package uk.gov.hmcts.cmc.email;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@EqualsAndHashCode
@Getter
public class EmailData {

    private final String to;

    private final String subject;

    private final String message;

    private final List<EmailAttachment> attachments;

    @Builder
    public EmailData(
        String to,
        String subject,
        String message,
        List<EmailAttachment> attachments
    ) {
        this.to = to;
        this.subject = subject;
        this.message = message;
        this.attachments = Collections.unmodifiableList(attachments);
    }

    public boolean hasAttachments() {
        return this.attachments != null && !this.attachments.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("EmailData{to='%s', subject='%s', message='%s', attachments=%s}",
            to, subject, message, attachments);
    }
}
