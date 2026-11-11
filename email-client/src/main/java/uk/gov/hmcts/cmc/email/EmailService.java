package uk.gov.hmcts.cmc.email;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.email.sendgrid.SendGridClient;

import java.io.IOException;

@Service
public class EmailService {

    public static final String NOTIFICATION_FAILURE = "Notification - failure";
    public static final String EMAIL_SUBJECT = "EmailSubject";
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final Logger telemetry;
    private final boolean asyncEventOperationEnabled;
    private final SendGridClient sendGridClient;

    @Autowired
    public EmailService(
        Logger telemetry,
        @Value("${feature_toggles.async_event_operations_enabled:false}") boolean asyncEventOperationEnabled,
        SendGridClient sendGridClient
    ) {
        this.telemetry = telemetry;
        this.asyncEventOperationEnabled = asyncEventOperationEnabled;
        this.sendGridClient = sendGridClient;
    }

    @Retryable(retryFor = EmailSendFailedException.class, backoff = @Backoff(delay = 100, maxDelay = 500))
    public void sendEmail(String from, EmailData emailData) {
        sendEmailSendGrid(from, emailData);
    }

    private void sendEmailSendGrid(String from, EmailData emailData) {
        try {
            sendGridClient.sendEmail(from, emailData);
        } catch (IOException e) {
            throw new EmailSendFailedException(e);
        }
        // sendGridClient may throw EmailSendFailedException directly - let it bubble past
    }

    @SuppressWarnings("unused")
    @Recover
    public void logSendMessageWithAttachmentFailure(
        EmailSendFailedException exception,
        String from,
        EmailData emailData
    ) {
        String errorMessage = String.format(
            "sendEmail failure:  failed to send email with details: %s due to %s",
            emailData.toString(), exception.getMessage()
        );
        logger.error(errorMessage, exception);

        telemetry.logRecordBuilder()
            .setSeverity(Severity.INFO)
            .setBody(NOTIFICATION_FAILURE)
            .setAllAttributes(Attributes.of(AttributeKey.stringKey(EMAIL_SUBJECT), emailData.getSubject()))
            .emit();
        if (asyncEventOperationEnabled) {
            throw exception;
        }
    }
}
