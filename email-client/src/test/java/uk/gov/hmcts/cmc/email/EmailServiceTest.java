package uk.gov.hmcts.cmc.email;

import com.launchdarkly.client.LDUser;
import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.cmc.email.sendgrid.SendGridClient;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;

import java.io.IOException;
import javax.mail.internet.MimeMessage;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private JavaMailSenderImpl javaMailSender;

    @Mock
    private TelemetryClient telemetryClient;

    @Mock
    private LaunchDarklyClient launchDarkly;

    @Mock
    private SendGridClient sendGrid;

    private EmailService emailService;

    @Mock
    private MimeMessage mimeMessage;

    @Before
    public void beforeEachTest() {
        emailService = new EmailService(telemetryClient, javaMailSender, false, sendGrid, launchDarkly);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    public void testSendEmailSuccess() {
        EmailData emailData = SampleEmailData.getDefault();
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        emailService.sendEmail(SampleEmailData.EMAIL_FROM, emailData);
        verify(javaMailSender).send(mimeMessage);
    }

    @Test(expected = RuntimeException.class)
    public void testSendEmailThrowsMailException() {
        EmailData emailData = SampleEmailData.getDefault();
        doThrow(mock(MailException.class)).when(javaMailSender).send(any(MimeMessage.class));
        emailService.sendEmail(SampleEmailData.EMAIL_FROM, emailData);

        verify(telemetryClient)
            .trackEvent(NOTIFICATION_FAILURE, singletonMap(EMAIL_SUBJECT, emailData.getSubject()), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendEmailThrowsInvalidArgumentExceptionForInvalidTo() {
        EmailData emailData = SampleEmailData.getWithToNull();
        emailService.sendEmail(SampleEmailData.EMAIL_FROM, emailData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendEmailThrowsInvalidArgumentExceptionForInvalidSubject() {
        EmailData emailData = SampleEmailData.getWithSubjectNull();
        emailService.sendEmail(SampleEmailData.EMAIL_FROM, emailData);
    }

    @Test(expected = EmailSendFailedException.class)
    public void shouldLogAndRaiseAppInsightWithAsyncEnabled() {
        EmailData emailData = SampleEmailData.getDefault();
        try {
            emailService = new EmailService(telemetryClient, javaMailSender, true, sendGrid, launchDarkly);
            emailService.logSendMessageWithAttachmentFailure(
                new EmailSendFailedException(new RuntimeException("Failed sending")),
                SampleEmailData.EMAIL_FROM,
                emailData
            );
        } finally {
            verify(telemetryClient).trackEvent(
                NOTIFICATION_FAILURE,
                singletonMap(EMAIL_SUBJECT, emailData.getSubject()),
                null);
        }
    }

    @Test
    public void shouldUseSendGridWhenToggledOn() throws IOException {
        when(launchDarkly.isFeatureEnabled(eq("sendgrid-roc-7497"), any(LDUser.class))).thenReturn(true);
        EmailData emailData = SampleEmailData.getDefault();
        emailService.sendEmail(SampleEmailData.EMAIL_FROM, emailData);
        verify(sendGrid).sendEmail(SampleEmailData.EMAIL_FROM, emailData);
    }

    @Test(expected = EmailSendFailedException.class)
    public void shouldWrapIOExceptionFromSendGridInEmailSendFailedException() throws IOException {
        when(launchDarkly.isFeatureEnabled(eq("sendgrid-roc-7497"), any(LDUser.class))).thenReturn(true);
        EmailData emailData = SampleEmailData.getDefault();
        doThrow(new IOException("expected exception")).when(sendGrid).sendEmail(anyString(), any(EmailData.class));
        emailService.sendEmail(SampleEmailData.EMAIL_FROM, emailData);
    }

}
