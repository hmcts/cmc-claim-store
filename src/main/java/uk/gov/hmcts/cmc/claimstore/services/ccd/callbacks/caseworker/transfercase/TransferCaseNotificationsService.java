package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.CaseTransferred.referenceForCaseTransferred;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.COURT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Service
public class TransferCaseNotificationsService {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    public TransferCaseNotificationsService(NotificationService notificationService,
                                            NotificationsProperties notificationsProperties) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    public void sendClaimUpdatedEmailToClaimant(Claim claim) {

        EmailTemplates templates = notificationsProperties.getTemplates().getEmail();

        notifyParty(claim, claim.getSubmitterEmail(),
            templates.getCaseTransferred(),
            "claimant");
    }

    public void sendClaimUpdatedEmailToDefendant(Claim claim) {

        EmailTemplates templates = notificationsProperties.getTemplates().getEmail();

        notifyParty(claim, claim.getDefendantEmail(),
            templates.getCaseTransferred(),
            "defendant");
    }

    private void notifyParty(Claim claim, String partyEmail, String partyEmailTemplateId, String party) {
        notificationService.sendMail(
            partyEmail,
            partyEmailTemplateId,
            aggregateParams(claim),
            referenceForCaseTransferred(claim.getReferenceNumber(), party)
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(EXTERNAL_ID, claim.getExternalId());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put(COURT_NAME, claim.getTransferContent().getNameOfTransferCourt());
        return parameters;
    }
}
