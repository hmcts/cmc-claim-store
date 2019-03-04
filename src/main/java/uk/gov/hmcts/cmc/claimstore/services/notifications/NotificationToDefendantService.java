package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ClaimantResponseSubmitted.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isResponseStatesPaid;

@Service
public class NotificationToDefendantService {
    private final Logger logger = LoggerFactory.getLogger(NotificationToDefendantService.class);

    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public NotificationToDefendantService(
        NotificationClient notificationClient,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationClient = notificationClient;
        this.notificationsProperties = notificationsProperties;
    }

    public void notifyDefendant(Claim claim) {
        Map<String, String> parameters = aggregateParams(claim);
        sendNotificationEmail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getResponseByClaimantEmailToDefendant(),
            parameters,
            referenceForDefendant(claim.getReferenceNumber())
        );
    }

    public void notifyDefendantOfRejection(Claim claim) {
        Map<String, String> parameters = aggregateParams(claim);
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        sendNotificationEmail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail()
                .getClaimantRejectedPartAdmitOrStatesPaidEmailToDefendant(),
            parameters,
            referenceForDefendant(claim.getReferenceNumber())
        );
    }

    public void notifyDefendantWhenInterlocutoryJudgementRequested(Claim claim) {
        Map<String, String> parameters = aggregateParams(claim);
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        sendNotificationEmail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantRequestedInterlocutoryJudgement(),
            parameters,
            referenceForDefendant(claim.getReferenceNumber())
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
        String targetEmail,
        String emailTemplate,
        Map<String, String> parameters,
        String reference
    ) {
        String errorMessage = String.format(
            "Failure: failed to send notification (%s) due to %s",
            reference, exception.getMessage()
        );

        logger.info(errorMessage, exception);
    }

    private Map<String, String> aggregateParams(Claim claim) {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());

        return parameters;
    }
}
