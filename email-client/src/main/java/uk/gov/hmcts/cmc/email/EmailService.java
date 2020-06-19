package uk.gov.hmcts.cmc.email;

import com.microsoft.applicationinsights.TelemetryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.email.sendgrid.SendGridClient;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;

import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static java.util.Collections.singletonMap;

@Service
public class EmailService {

    public static final String NOTIFICATION_FAILURE = "Notification - failure";
    public static final String EMAIL_SUBJECT = "EmailSubject";
    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final TelemetryClient telemetryClient;
    private final JavaMailSender sender;
    private final boolean asyncEventOperationEnabled;
    private final SendGridClient sendGridClient;
    private final LaunchDarklyClient launchDarklyClient;

    @Autowired
    public EmailService(
        TelemetryClient telemetryClient,
        JavaMailSender sender,
        @Value("${feature_toggles.async_event_operations_enabled:false}") boolean asyncEventOperationEnabled,
        SendGridClient sendGridClient,
        LaunchDarklyClient launchDarklyClient
    ) {
        this.telemetryClient = telemetryClient;
        this.sender = sender;
        this.asyncEventOperationEnabled = asyncEventOperationEnabled;
        this.sendGridClient = sendGridClient;
        this.launchDarklyClient = launchDarklyClient;
    }

    @Retryable(value = EmailSendFailedException.class, backoff = @Backoff(delay = 100, maxDelay = 500))
    public void sendEmail(String from, EmailData emailData) {
        if (launchDarklyClient.isFeatureEnabled("sendgrid-roc-7497", LaunchDarklyClient.CLAIM_STORE_USER)) {
            sendEmailSendGrid(from, emailData);
        } else {
            sendEmailMTA(from, emailData);
        }
    }

    private void sendEmailMTA(String from, EmailData emailData) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);

            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(emailData.getTo());
            mimeMessageHelper.setSubject(emailData.getSubject());
            mimeMessageHelper.setText(emailData.getMessage());
            if (emailData.hasAttachments()) {
                for (EmailAttachment emailAttachment : emailData.getAttachments()) {
                    mimeMessageHelper.addAttachment(emailAttachment.getFilename(),
                        emailAttachment.getData(),
                        emailAttachment.getContentType());
                }
            }
            sender.send(message);
        } catch (MessagingException | MailException e) {
            throw new EmailSendFailedException(e);
        }
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

        telemetryClient.trackEvent(NOTIFICATION_FAILURE, singletonMap(EMAIL_SUBJECT, emailData.getSubject()), null);
        if (asyncEventOperationEnabled) {
            throw exception;
        }
    }
}
