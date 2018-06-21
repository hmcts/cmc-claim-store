package uk.gov.hmcts.cmc.claimstore.services.notifications;

import com.google.common.collect.ImmutableMap;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
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
public class ResponseNeededNotificationService {
    private final Logger logger = LoggerFactory.getLogger(ResponseNeededNotificationService.class);

    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public ResponseNeededNotificationService(
        NotificationClient notificationClient,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationClient = notificationClient;
        this.notificationsProperties = notificationsProperties;
    }

    @Retryable(value = NotificationException.class, backoff = @Backoff(delay = 200))
    public void sendMail(JobDetail jobDetail) throws JobExecutionException {
        JobDataMap emailData = jobDetail.getJobDataMap();
        Map<String, String> parameters = aggregateParams(emailData);
        try {
            notificationClient.sendEmail(
                getEmailTemplates().getDefendantResponseNeeded(),
                (String) emailData.get("defendantEmail"),
                parameters,
                (String) emailData.get("caseReference")
            );
        } catch (NotificationClientException e) {
            throw new JobExecutionException(e);
        }
    }

    @Recover
    public void logNotificationFailure(
        JobExecutionException exception,
        JobDetail jobDetail
    ) throws JobExecutionException {
        JobDataMap emailData = jobDetail.getJobDataMap();
        logger.info("Failure: failed to send response needed notification ({} to defendant at {}) due to {}",
            emailData.get("caseReference"),
            emailData.get("defendantEmail"),
            exception.getMessage(),
            exception);

        exception.setRefireImmediately(true);
        throw exception;
    }

    private Map<String, String> aggregateParams(Map<String, Object> emailData) {
        return new ImmutableMap.Builder<String, String>()
            .put(CLAIM_REFERENCE_NUMBER, (String) emailData.get("caseReference"))
            .put(CLAIMANT_NAME, (String) emailData.get("claimantName"))
            .put(DEFENDANT_NAME, (String) emailData.get("defendantName"))
            .put(RESPONSE_DEADLINE, Formatting.formatDate((LocalDate) emailData.get("responseDeadline")))
            .put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl())
            .put(RESPOND_TO_CLAIM_URL, notificationsProperties.getRespondToClaimUrl())
            .build();
    }

    private EmailTemplates getEmailTemplates() {
        NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
