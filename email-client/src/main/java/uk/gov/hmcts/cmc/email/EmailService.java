package uk.gov.hmcts.cmc.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender sender;

    @Autowired
    public EmailService(final JavaMailSender sender) {
        this.sender = sender;
    }

    @Retryable(value = EmailSendFailedException.class, backoff = @Backoff(delay = 100, maxDelay = 500))
    public void sendEmail(final String from, final EmailData emailData) {
        try {
            final MimeMessage message = sender.createMimeMessage();
            final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);

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

    @Recover
    public void logSendMessageWithAttachmentFailure(final RuntimeException exception, final EmailData emailData) {
        final String errorMessage = String.format(
            "sendEmail failure:  failed to send email with details: %s due to %s",
            emailData.toString(), exception.getMessage()
        );
        logger.error(errorMessage, exception);
    }
}
