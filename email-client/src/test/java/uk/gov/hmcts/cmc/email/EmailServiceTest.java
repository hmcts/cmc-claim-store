package uk.gov.hmcts.cmc.email;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.cmc.email.sendgrid.SendGridClient;

import java.io.IOException;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.email.EmailService.EMAIL_SUBJECT;
import static uk.gov.hmcts.cmc.email.EmailService.NOTIFICATION_FAILURE;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class EmailServiceTest {

    @Mock
    private TelemetryClient telemetryClient;

    @Mock
    private SendGridClient sendGrid;

    private EmailService emailService;

    @Before
    public void beforeEachTest() {
        emailService = new EmailService(telemetryClient, false, sendGrid);
    }

    @Test
    public void shouldUseSendGrid() throws IOException {
        EmailData emailData = SampleEmailData.getDefault();
        emailService.sendEmail(SampleEmailData.EMAIL_FROM, emailData);
        verify(sendGrid).sendEmail(SampleEmailData.EMAIL_FROM, emailData);
    }

    @Test(expected = EmailSendFailedException.class)
    public void shouldWrapIOExceptionInEmailSendFailedException() throws IOException {
        EmailData emailData = SampleEmailData.getDefault();
        doThrow(new IOException("expected exception")).when(sendGrid).sendEmail(anyString(), any(EmailData.class));
        emailService.sendEmail(SampleEmailData.EMAIL_FROM, emailData);

        verify(telemetryClient)
            .trackEvent(NOTIFICATION_FAILURE, singletonMap(EMAIL_SUBJECT, emailData.getSubject()), null);
    }

}
