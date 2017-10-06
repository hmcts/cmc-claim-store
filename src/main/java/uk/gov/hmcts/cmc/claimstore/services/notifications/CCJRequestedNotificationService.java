package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotificationException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Service
public class CCJRequestedNotificationService {
    private final Logger logger = LoggerFactory.getLogger(CCJRequestedNotificationService.class);

    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public CCJRequestedNotificationService(
        final NotificationClient notificationClient,
        final NotificationsProperties notificationsProperties
    ) {
        this.notificationClient = notificationClient;
        this.notificationsProperties = notificationsProperties;
    }

    public void notifyClaimant(final Claim claim) {
        final Map<String, String> parameters = aggregateParams(claim);
        sendNotificationEmail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantCCJRequested(),
            parameters,
            NotificationReferenceBuilder.CCJRequested.referenceForClaimant(claim.getReferenceNumber())
        );
    }

    public void notifyDefendant(final Claim claim) {
        final Map<String, String> parameters = aggregateParams(claim);
        sendNotificationEmail(
            claim.getClaimData().getDefendant().getEmail()
                .orElseThrow(() -> new NotificationException(("Unknown defendant email"))),
            notificationsProperties.getTemplates().getEmail().getDefendantCCJRequested(),
            parameters,
            NotificationReferenceBuilder.CCJRequested.referenceForDefendant(claim.getReferenceNumber())
        );
    }

    @Retryable(value = NotificationException.class, backoff = @Backoff(delay = 200))
    public void sendNotificationEmail(
        final String targetEmail,
        final String emailTemplate,
        final Map<String, String> parameters,
        final String reference
    ) {
        try {
            notificationClient.sendEmail(emailTemplate, targetEmail, parameters, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }

    @Recover
    public void logNotificationFailure(
        final NotificationException exception,
        final Claim claim,
        final String targetEmail,
        final String emailTemplate,
        final String reference
    ) {
        final String errorMessage = String.format(
            "Failure: failed to send notification ( %s to %s ) due to %s",
            reference, targetEmail, exception.getMessage()
        );

        logger.info(errorMessage, exception);
    }

    private Map<String, String> aggregateParams(final Claim claim) {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put("ccjRequestedDate", Formatting.formatDate(claim.getCountyCourtJudgmentRequestedAt()));

        return parameters;
    }
}
