package uk.gov.hmcts.cmc.email;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.cmc.email.EmailService.EMAIL_SUBJECT;

public class SampleEmailData {

    public static final String EMAIL_FROM = "no-reply@example.com";
    public static final String EMAIL_TO = "user@example.com";
    public static final String EMAIL_MESSAGE = "My Test Message";

    public static EmailData getDefault() {
        return new EmailData(EMAIL_TO, EMAIL_SUBJECT, EMAIL_MESSAGE, Collections.emptyList());
    }

    public static EmailData getWithToNull() {
        return new EmailData(null, EMAIL_SUBJECT, EMAIL_MESSAGE, Collections.emptyList());
    }

    public static EmailData getWithSubjectNull() {
        return new EmailData(EMAIL_TO, null, EMAIL_MESSAGE, Collections.emptyList());
    }

    public static EmailData getWithAttachment(String pdfFilename) {
        return new EmailData(EMAIL_TO, EMAIL_SUBJECT, EMAIL_MESSAGE,
            List.of(EmailAttachment.pdf(new byte[] {1, 2, 3}, pdfFilename)));
    }

    public static EmailData getWithEmptyContent() {
        return new EmailData(EMAIL_TO, EMAIL_SUBJECT, "", Collections.emptyList());
    }

    private SampleEmailData() {
        // hidden constructor
    }
}
