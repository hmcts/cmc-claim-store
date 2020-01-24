package uk.gov.hmcts.cmc.email;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import javax.mail.internet.MimeMessage;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.email.EmailService.EMAIL_SUBJECT;
import static uk.gov.hmcts.cmc.email.EmailService.NOTIFICATION_FAILURE;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class EmailServiceTest {

    private static final String EMAIL_TO = "user@example.com";
    private static final String EMAIL_MESSAGE = "My Test Message";

    @Mock
    private JavaMailSenderImpl javaMailSender;

    @Mock
    private TelemetryClient telemetryClient;

    private EmailService emailService;

    @Mock
    private MimeMessage mimeMessage;

    @Before
    public void beforeEachTest() {
        emailService = new EmailService(telemetryClient, javaMailSender, false);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    public void testSendEmailSuccess() {
        EmailData emailData = SampleEmailData.getDefault();
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        emailService.sendEmail("no-reply@example.com", emailData);
        verify(javaMailSender).send(mimeMessage);
    }

    @Test(expected = RuntimeException.class)
    public void testSendEmailThrowsMailException() {
        EmailData emailData = SampleEmailData.getDefault();
        doThrow(mock(MailException.class)).when(javaMailSender).send(any(MimeMessage.class));
        emailService.sendEmail("no-reply@example.com", emailData);

        verify(telemetryClient)
            .trackEvent(NOTIFICATION_FAILURE, singletonMap(EMAIL_SUBJECT, emailData.getSubject()), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendEmailThrowsInvalidArgumentExceptionForInvalidTo() {
        EmailData emailData = SampleEmailData.getWithToNull();
        emailService.sendEmail("no-reply@example.com", emailData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendEmailThrowsInvalidArgumentExceptionForInvalidSubject() {
        EmailData emailData = SampleEmailData.getWithSubjectNull();
        emailService.sendEmail("no-reply@example.com", emailData);
    }

    @Test(expected = EmailSendFailedException.class)
    public void shouldLogAndRaiseAppInsightWithAsyncEnabled() {
        EmailData emailData = SampleEmailData.getDefault();
        try {
            emailService = new EmailService(telemetryClient, javaMailSender, true);
            emailService.logSendMessageWithAttachmentFailure(
                new EmailSendFailedException(new RuntimeException("Failed sending")),
                "no-reply@example.com",
                emailData
            );
        } finally {
            verify(telemetryClient).trackEvent(
                NOTIFICATION_FAILURE,
                singletonMap(EMAIL_SUBJECT, emailData.getSubject()),
                null);
        }
    }

    public static class SampleEmailData {

        static EmailData getDefault() {
            return new EmailData(EMAIL_TO, EMAIL_SUBJECT, EMAIL_MESSAGE, Collections.emptyList());
        }

        static EmailData getWithToNull() {
            return new EmailData(null, EMAIL_SUBJECT, EMAIL_MESSAGE, Collections.emptyList());
        }

        static EmailData getWithSubjectNull() {
            return new EmailData(EMAIL_TO, null, EMAIL_MESSAGE, Collections.emptyList());
        }
    }
}
