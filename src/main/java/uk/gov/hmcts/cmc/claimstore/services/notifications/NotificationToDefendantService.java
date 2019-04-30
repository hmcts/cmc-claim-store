package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ClaimantResponseSubmitted.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Service
public class NotificationToDefendantService {
    private final Logger logger = LoggerFactory.getLogger(NotificationToDefendantService.class);

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public NotificationToDefendantService(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    public void notifyDefendant(Claim claim) {
        Map<String, String> parameters = aggregateParams(claim);
        notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getResponseByClaimantEmailToDefendant(),
            parameters,
            referenceForDefendant(claim.getReferenceNumber())
        );
    }

    public void notifyDefendantOfRejection(Claim claim) {
        Map<String, String> parameters = aggregateParams(claim);
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        notificationService.sendMail(
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
        notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantRequestedInterlocutoryJudgement(),
            parameters,
            referenceForDefendant(claim.getReferenceNumber())
        );

    }

    private Map<String, String> aggregateParams(Claim claim) {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());

        return parameters;
    }
}
