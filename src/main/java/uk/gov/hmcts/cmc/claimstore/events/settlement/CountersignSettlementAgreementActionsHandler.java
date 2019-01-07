package uk.gov.hmcts.cmc.claimstore.events.settlement;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Component
public class CountersignSettlementAgreementActionsHandler {
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public CountersignSettlementAgreementActionsHandler(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @EventListener
    public void sendNotificationToClaimant(CountersignSettlementAgreementEvent event) {
        final Claim claim = event.getClaim();
        final Map<String, String> parameters = aggregateParams(claim);
        final String referenceNumber = claim.getReferenceNumber();
        this.notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getDefendantSignedSettlementAgreementToClaimant(),
            parameters,
            NotificationReferenceBuilder.AgreementCounterSigned.referenceForClaimant(referenceNumber,
                NotificationReferenceBuilder.DEFENDANT)
        );
    }

    @EventListener
    public void sendNotificationToDefendant(CountersignSettlementAgreementEvent event) {
        final Claim claim = event.getClaim();
        final Map<String, String> parameters = aggregateParams(claim);
        final String referenceNumber = claim.getReferenceNumber();
        this.notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getDefendantSignedSettlementAgreementToDefendant(),
            parameters,
            NotificationReferenceBuilder.AgreementCounterSigned.referenceForDefendant(referenceNumber,
                NotificationReferenceBuilder.DEFENDANT)
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {
        return ImmutableMap.of(
            CLAIMANT_NAME, claim.getClaimData().getClaimant().getName(),
            DEFENDANT_NAME, claim.getClaimData().getDefendant().getName(),
            FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl(),
            CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber()
        );
    }

}
