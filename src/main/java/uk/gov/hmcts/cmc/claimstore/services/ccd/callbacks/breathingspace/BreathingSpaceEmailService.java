package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;

@Service
public class BreathingSpaceEmailService {
    private final NotificationService notificationService;

    public BreathingSpaceEmailService(
        NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    public void sendEmailNotificationToDefendant(Claim claim, String templateId) {
        notificationService.sendMail(
            claim.getDefendantEmail(),
            templateId,
            prepareNotificationParameters(claim),
            referenceForDefendant(claim.getReferenceNumber())
        );
    }

    public void sendNotificationToClaimant(Claim claim, String templateId) {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            templateId,
            prepareNotificationParameters(claim),
            referenceForClaimant(claim.getReferenceNumber())
        );
    }

    private Map<String, String> prepareNotificationParameters(Claim claim) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber(),
            CLAIMANT_NAME, claim.getClaimData().getClaimant().getName(),
            DEFENDANT_NAME, claim.getClaimData().getDefendant().getName()
        );
    }
}
