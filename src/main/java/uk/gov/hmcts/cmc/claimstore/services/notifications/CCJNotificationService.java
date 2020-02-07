package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Service
public class CCJNotificationService {

    private final NotificationsProperties notificationsProperties;
    private final NotificationService notificationService;

    @Autowired
    public CCJNotificationService(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    public void notifyClaimantAboutCCJReminder(Claim claim) {
        Map<String, String> parameters = aggregateParams(claim);
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantCCJReminder(),
            parameters,
            NotificationReferenceBuilder.CCJRequested.reminderForClaimant(claim.getReferenceNumber())
        );
    }

    public void notifyClaimantForCCJRequest(Claim claim) {
        Map<String, String> parameters = aggregateParams(claim);
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantCCJRequested(),
            parameters,
            NotificationReferenceBuilder.CCJRequested.referenceForClaimant(claim.getReferenceNumber())
        );
    }

    public void notifyDefendantForCCJRequested(Claim claim) {
        Map<String, String> parameters = aggregateParams(claim);
        notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getResponseByClaimantEmailToDefendant(),
            parameters,
            NotificationReferenceBuilder.CCJIssued.referenceForDefendant(claim.getReferenceNumber())
        );
    }

    public void notifyClaimantForRedeterminationRequest(Claim claim) {
        Map<String, String> parameters = aggregateParams(claim);
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getRedeterminationEmailToClaimant(),
            parameters,
            NotificationReferenceBuilder.RedeterminationRequested.referenceForClaimant(claim.getReferenceNumber())
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put("ccjRequestedDate", Formatting.formatDate(claim.getCountyCourtJudgmentRequestedAt()));
        return parameters;
    }
}
