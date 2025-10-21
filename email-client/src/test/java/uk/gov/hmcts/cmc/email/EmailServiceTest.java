package uk.gov.hmcts.cmc.email;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.cmc.email.sendgrid.SendGridClient;

import java.io.IOException;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.email.EmailService.EMAIL_SUBJECT;
import static uk.gov.hmcts.cmc.email.EmailService.NOTIFICATION_FAILURE;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration
public class EmailServiceTest {

    @Mock
    private TelemetryClient telemetryClient;

    @Mock
    private SendGridClient sendGrid;

    private EmailService emailService;

    @BeforeEach
    public void beforeEachTest() {
        emailService = new EmailService(telemetryClient, false, sendGrid);
    }

    @Test
    public void testSendGridEmailSuccess() throws IOException {
        EmailData emailData = SampleEmailData.getDefault();
        emailService.sendEmail(SampleEmailData.EMAIL_FROM, emailData);
        verify(sendGrid).sendEmail(SampleEmailData.EMAIL_FROM, emailData);
    }

    @Test
    public void shouldLogAndRaiseAppInsightWithAsyncEnabled() {
        EmailData emailData = SampleEmailData.getDefault();
        try {
            emailService = new EmailService(telemetryClient, true, sendGrid);
            assertThrows(EmailSendFailedException.class, () -> {
                emailService.logSendMessageWithAttachmentFailure(
                    new EmailSendFailedException(new RuntimeException("Failed sending")),
                    SampleEmailData.EMAIL_FROM,
                    emailData
                );
            });
        } finally {
            verify(telemetryClient).trackEvent(
                NOTIFICATION_FAILURE,
                singletonMap(EMAIL_SUBJECT, emailData.getSubject()),
                null);
        }
    }

    @Test
    public void shouldWrapIOExceptionFromSendGridInEmailSendFailedException() throws IOException {
        EmailData emailData = SampleEmailData.getDefault();
        doThrow(new IOException("expected exception")).when(sendGrid).sendEmail(anyString(), any(EmailData.class));

        assertThrows(EmailSendFailedException.class, () -> {
            emailService.sendEmail(SampleEmailData.EMAIL_FROM, emailData);
        });

        verify(telemetryClient)
            .trackEvent(NOTIFICATION_FAILURE, singletonMap(EMAIL_SUBJECT, emailData.getSubject()), null);
    }

}
