package uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Service
public class OrderDrawnNotificationService {
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public OrderDrawnNotificationService(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    public void notifyDefendant(CCDCase ccdCase) {
        Map<String, String> parameters = ImmutableMap.of(
            CLAIM_REFERENCE_NUMBER, ccdCase.getPreviousServiceCaseReference(),
            DEFENDANT_NAME, ccdCase.getRespondents().get(0).getValue().getPartyName(),
            FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl()
        );
        notificationService.sendMail(
            ccdCase.getRespondents().get(0).getValue().getPartyDetail().getEmailAddress(),
            notificationsProperties.getTemplates().getEmail().getDefendantLegalOrderDrawn(),
            parameters,
            NotificationReferenceBuilder.LegalOrderDrawn.referenceForDefendant(
                ccdCase.getPreviousServiceCaseReference())
        );
    }

    public void notifyClaimant(CCDCase ccdCase) {
        Map<String, String> parameters = ImmutableMap.of(
            CLAIM_REFERENCE_NUMBER, ccdCase.getPreviousServiceCaseReference(),
            CLAIMANT_NAME, ccdCase.getApplicants().get(0).getValue().getPartyName(),
            FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl()
        );
        notificationService.sendMail(
            ccdCase.getApplicants().get(0).getValue().getPartyDetail().getEmailAddress(),
            notificationsProperties.getTemplates().getEmail().getClaimantLegalOrderDrawn(),
            parameters,
            NotificationReferenceBuilder.LegalOrderDrawn.referenceForClaimant(
                ccdCase.getPreviousServiceCaseReference())
        );
    }

}
