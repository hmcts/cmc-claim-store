package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ContactDetailsChanged;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Service
public class ChangeContactDetailsNotificationService {
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public ChangeContactDetailsNotificationService(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    public void sendEmailToRightRecipient(CCDCase ccdCase, Claim claim) {
        EmailTemplates templates = notificationsProperties.getTemplates().getEmail();
        if (ccdCase.getContactChangeParty() == CCDContactPartyType.CLAIMANT) {
            notifyParty(claim, claim.getDefendantEmail(),
                templates.getClaimantContactDetailsChanged(), "defendant");
        } else {
            notifyParty(claim, claim.getSubmitterEmail(),
                templates.getDefendantContactDetailsChanged(), "claimant");
        }
    }

    private void notifyParty(Claim claim, String partyEmail, String partyEmailTemplateId, String party) {
        notificationService.sendMail(
            partyEmail,
            partyEmailTemplateId,
            aggregateParams(claim),
            ContactDetailsChanged.referenceForContactChanges(claim.getReferenceNumber(), party)
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(EXTERNAL_ID, claim.getExternalId());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        return parameters;
    }
}
