package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

@Component
public class MoreTimeRequestedNotificationService {
    private final Logger logger = LoggerFactory.getLogger(MoreTimeRequestedNotificationService.class);
    private final NotificationClient notificationClient;

    @Autowired
    public MoreTimeRequestedNotificationService(final NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    @Retryable(value = NotificationException.class, backoff = @Backoff(delay = 200))
    public void sendMail(
        final String targetEmail,
        final String emailTemplateId,
        final Map<String, String> parameters,
        final String reference
    ) {
        try {
            notificationClient.sendEmail(emailTemplateId, targetEmail, parameters, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }

    @Recover
    public void logNotificationFailure(
        final NotificationException exception,
        final String targetEmail,
        final String emailTemplateId,
        final Map<String, String> parameters,
        final String reference
    ) {
        final String errorMessage = String.format(
            "Failure: failed to send notification (%s to %s) due to %s",
            reference,
            targetEmail,
            exception.getMessage()
        );

        logger.info(errorMessage, exception);
    }
}
