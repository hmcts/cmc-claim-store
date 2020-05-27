package uk.gov.hmcts.cmc.email;

import java.util.Collections;

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
}
