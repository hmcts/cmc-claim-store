package uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.EmailUtils;

import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Service
public class OrderDrawnNotificationService {
    private final Logger logger = LoggerFactory.getLogger(OrderDrawnNotificationService.class);

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public OrderDrawnNotificationService(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    public void notifyDefendant(Claim claim) {
        EmailUtils.getDefendantEmail(claim).ifPresent(
            defendantEmail -> {
                logger.info("Sending order drawn email to defendant");
                Map<String, String> parameters = Map.of(
                    CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber(),
                    DEFENDANT_NAME, claim.getClaimData().getDefendant().getName(),
                    FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());

                notificationService.sendMail(
                    defendantEmail,
                    notificationsProperties.getTemplates().getEmail().getDefendantLegalOrderDrawn(),
                    parameters,
                    NotificationReferenceBuilder.LegalOrderDrawn.referenceForDefendant(
                        claim.getReferenceNumber())
                );
            }
        );

    }

    public void notifyClaimant(Claim claim) {
        Map<String, String> parameters = Map.of(
            CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber(),
            CLAIMANT_NAME, claim.getClaimData().getClaimant().getName(),
            FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl()
        );

        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantLegalOrderDrawn(),
            parameters,
            NotificationReferenceBuilder.LegalOrderDrawn.referenceForClaimant(
                claim.getReferenceNumber())
        );
    }

}
