package uk.gov.hmcts.cmc.email;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.cmc.email.sendgrid.SendGridClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.email.EmailService.EMAIL_SUBJECT;
import static uk.gov.hmcts.cmc.email.EmailService.NOTIFICATION_FAILURE;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration
public class EmailServiceTest {

    @Mock
    private Logger telemetry;

    @Mock
    private LogRecordBuilder logRecord;

    @Mock
    private SendGridClient sendGrid;

    private EmailService emailService;

    @BeforeEach
    public void beforeEachTest() {
        lenient().when(telemetry.logRecordBuilder()).thenReturn(logRecord);
        lenient().when(logRecord.setSeverity(any(Severity.class))).thenReturn(logRecord);
        lenient().when(logRecord.setBody(anyString())).thenReturn(logRecord);
        lenient().when(logRecord.setAllAttributes(any(Attributes.class))).thenReturn(logRecord);
        emailService = new EmailService(telemetry, false, sendGrid);
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
            emailService = new EmailService(telemetry, true, sendGrid);
            assertThrows(EmailSendFailedException.class, () -> {
                emailService.logSendMessageWithAttachmentFailure(
                    new EmailSendFailedException(new RuntimeException("Failed sending")),
                    SampleEmailData.EMAIL_FROM,
                    emailData
                );
            });
        } finally {
            verify(logRecord).setBody(NOTIFICATION_FAILURE);
            verify(logRecord).setAllAttributes(Attributes.of(
                AttributeKey.stringKey(EMAIL_SUBJECT), emailData.getSubject()));
            verify(logRecord).emit();
        }
    }

    @Test
    public void shouldWrapIOExceptionFromSendGridInEmailSendFailedException() throws IOException {
        EmailData emailData = SampleEmailData.getDefault();
        doThrow(new IOException("expected exception")).when(sendGrid).sendEmail(anyString(), any(EmailData.class));

        assertThrows(EmailSendFailedException.class, () -> {
            emailService.sendEmail(SampleEmailData.EMAIL_FROM, emailData);
        });
    }

}
