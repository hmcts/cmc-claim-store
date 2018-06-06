package uk.gov.hmcts.cmc.claimstore.services.notifications;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.scheduler.services.ResponseNeededNotification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.RESPOND_TO_CLAIM_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.RESPONSE_DEADLINE;

@Service
public class DefendantResponseNeededNotificationService implements ResponseNeededNotification {
    private final Logger logger = LoggerFactory.getLogger(DefendantResponseNeededNotificationService.class);

    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public DefendantResponseNeededNotificationService(
        NotificationClient notificationClient,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationClient = notificationClient;
        this.notificationsProperties = notificationsProperties;
    }

    @Retryable(value = NotificationException.class, backoff = @Backoff(delay = 200))
    @Override
    public void sendMail(Map<String, Object> emailData) {
        Map<String, String> parameters = aggregateParams(emailData);
        try {
            notificationClient.sendEmail(
                getEmailTemplates().getDefendantResponseNeeded(),
                (String) emailData.get("defendantEmail"),
                parameters,
                (String) emailData.get("caseReference")
            );
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }

    @Recover
    public void logNotificationFailure(
        NotificationException exception,
        Map<String, Object> emailData
    ) {
        String errorMessage = "Failure: "
            + " failed to send defendant response needed notification (" + emailData.get("caseReference")
            + " to " + emailData.get("defendantEmail") + ") "
            + " due to " + exception.getMessage();

        logger.info(errorMessage, exception);
    }

    private Map<String, String> aggregateParams(Map<String, Object> emailData) {
        ImmutableMap.Builder<String, String> parameters = new ImmutableMap.Builder<>();
        parameters.put(CLAIM_REFERENCE_NUMBER, (String) emailData.get("caseReference"));

        parameters.put(CLAIMANT_NAME, (String) emailData.get("claimantName"));
        parameters.put(DEFENDANT_NAME, (String) emailData.get("defendantName"));
        parameters.put(RESPONSE_DEADLINE, Formatting.formatDate((LocalDate) emailData.get("responseDeadline")));
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(RESPOND_TO_CLAIM_URL, notificationsProperties.getRespondToClaimUrl());
        return parameters.build();
    }

    private EmailTemplates getEmailTemplates() {
        NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
