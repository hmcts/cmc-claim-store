package uk.gov.hmcts.cmc.claimstore.services.notifications;

import com.google.common.collect.ImmutableMap;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.RESPOND_TO_CLAIM_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.RESPONSE_DEADLINE;

@Service
public class ResponseNeededNotificationService {
    private static final String LOG_TEMPLATE = "Response needed notification cannot be sent ({}) due to {}";

    private final Logger logger = LoggerFactory.getLogger(ResponseNeededNotificationService.class);

    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;
    private final AppInsights appInsights;
    private final ClaimService claimService;

    @Autowired
    public ResponseNeededNotificationService(
        NotificationClient notificationClient,
        NotificationsProperties notificationsProperties,
        AppInsights appInsights,
        ClaimService claimService) {
        this.notificationClient = notificationClient;
        this.notificationsProperties = notificationsProperties;
        this.appInsights = appInsights;
        this.claimService = claimService;
    }

    @Retryable(value = NotificationException.class, backoff = @Backoff(delay = 200))
    public void sendMail(JobDetail jobDetail) {
        JobDataMap jobData = jobDetail.getJobDataMap();
        String caseReference = (String) jobData.get("caseReference");

        claimService.getClaimByReferenceAnonymous(caseReference)
            .ifPresent(claim -> {
                if (claim.getRespondedAt() == null) {
                    if (claim.getDefendantEmail() == null) {
                        logger.warn(LOG_TEMPLATE, caseReference, "missing defendant email address");
                    } else {
                        try {
                            notificationClient.sendEmail(
                                getEmailTemplates().getDefendantResponseNeeded(),
                                claim.getDefendantEmail(),
                                aggregateParams(claim),
                                caseReference
                            );
                        } catch (NotificationClientException e) {
                            throw new NotificationException(e);
                        }
                    }
                }
            });
    }

    @Recover
    public void logNotificationFailure(
        NotificationException exception,
        JobDetail jobDetail
    ) {
        JobDataMap emailData = jobDetail.getJobDataMap();
        String caseReference = (String) emailData.get("caseReference");
        logger.error(LOG_TEMPLATE, caseReference, exception.getMessage(), exception);

        appInsights.trackEvent(AppInsightsEvent.SCHEDULER_JOB_FAILED, REFERENCE_NUMBER, caseReference);
    }

    private Map<String, String> aggregateParams(Claim claim) {
        String defendantName = claim.getClaimData().getDefendant().getName();
        String claimantName = claim.getClaimData().getClaimant().getName();

        return new ImmutableMap.Builder<String, String>()
            .put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber())
            .put(CLAIMANT_NAME, claimantName)
            .put(DEFENDANT_NAME, defendantName)
            .put(RESPONSE_DEADLINE, Formatting.formatDate(claim.getResponseDeadline()))
            .put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl())
            .put(RESPOND_TO_CLAIM_URL, notificationsProperties.getRespondToClaimUrl())
            .build();
    }

    private EmailTemplates getEmailTemplates() {
        NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
