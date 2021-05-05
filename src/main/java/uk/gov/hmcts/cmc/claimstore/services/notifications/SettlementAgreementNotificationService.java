package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ClaimantResponseSubmitted.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ClaimantResponseSubmitted.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.RESPONSE_DEADLINE;

@Service
public class SettlementAgreementNotificationService {
    private final Logger logger = LoggerFactory.getLogger(SettlementAgreementNotificationService.class);

    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;
    private final AppInsights appInsights;

    @Autowired
    public SettlementAgreementNotificationService(
        NotificationClient notificationClient,
        NotificationsProperties notificationsProperties,
        AppInsights appInsights
    ) {
        this.notificationClient = notificationClient;
        this.notificationsProperties = notificationsProperties;
        this.appInsights = appInsights;
    }

    public void notifyDefendant(Claim claim) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put(RESPONSE_DEADLINE, Formatting.formatDate(LocalDate.now().plusDays(7)));
        sendNotificationEmail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantSignedSettlementAgreementToDefendant(),
            parameters,
            referenceForDefendant(claim.getReferenceNumber())
        );
    }

    public void notifyClaimant(Claim claim) {
        Map<String, String> parameters = Map.of(
            CLAIMANT_NAME, claim.getClaimData().getClaimant().getName(),
            FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl(),
            CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());

        sendNotificationEmail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantSignedSettlementAgreementToClaimant(),
            parameters,
            referenceForClaimant(claim.getReferenceNumber())
        );
    }

    @Retryable(value = NotificationException.class, backoff = @Backoff(delay = 200))
    public void sendNotificationEmail(
        String targetEmail,
        String emailTemplate,
        Map<String, String> parameters,
        String reference
    ) {
        try {
            notificationClient.sendEmail(emailTemplate, targetEmail, parameters, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }

    @Recover
    public void logNotificationFailure(
        NotificationException exception,
        String emailTemplate,
        String targetEmail,
        Map<String, String> parameters,
        String reference
    ) {
        String errorMessage = String.format(
            "Failure: failed to send notification (%s) due to %s",
            reference, exception.getMessage()
        );

        logger.warn(errorMessage, exception);
        appInsights.trackEvent(AppInsightsEvent.NOTIFICATION_FAILURE, REFERENCE_NUMBER, reference);
    }

}
