package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CORRESPONDENCE_ADDRESS_CHANGED;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CORRESPONDENCE_ADDRESS_REMOVED;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.EMAIL_ADDRESS_CHANGED;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.EMAIL_ADDRESS_REMOVED;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.MAIN_ADDRESS_CHANGED;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.PHONE_NUMBER_CHANGED;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.PHONE_NUMBER_REMOVED;

@Service
public class ChangeContactDetailsNotificationService {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    private static final String CLAIMANT = "Claimant";
    private static final String DEFENDANT = "Defendant";

    @Autowired
    public ChangeContactDetailsNotificationService(
            NotificationService notificationService,
            NotificationsProperties notificationsProperties) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }


//    public void sendEmailToRightRecipient(CCDCase ccdCase, Claim claim){
//        if (ccdCase.getChangeContactDetailsForParty().getValue().equals(CLAIMANT)) {
//            notifyDefendant(claim, );
//        } else if (ccdCase.getChangeContactDetailsForParty().getValue().equals(DEFENDANT)) {
//            notifyClaimant(claim);
//        }
//    }

    private void notifyClaimant(Claim claim, Boolean address, Boolean phone, Boolean email,
                                Boolean corraddress, Boolean phoneRemoved,
                                Boolean corrAddressRemoved, Boolean emailRemoved) {
        notificationService.sendMail(
                claim.getSubmitterEmail(),
                notificationsProperties.getTemplates().getEmail().getDefendantContactDetailsChanged(),
                aggregateParams(claim, address, phone, email, corraddress, phoneRemoved, corrAddressRemoved, emailRemoved),
                NotificationReferenceBuilder.ContactDetailsChanged
                        .referenceForClaimant(claim.getReferenceNumber(), "defendant")
        );
    }

    private void notifyDefendant(Claim claim, Boolean address, Boolean phone, Boolean email,
                                 Boolean corraddress, Boolean phoneRemoved,
                                 Boolean corrAddressRemoved, Boolean emailRemoved) {
        notificationService.sendMail(
                claim.getDefendantEmail(),
                notificationsProperties.getTemplates().getEmail().getClaimantContactDetailsChanged(),
                aggregateParams(claim, address, phone, email, corraddress, phoneRemoved, corrAddressRemoved, emailRemoved),
                NotificationReferenceBuilder.ContactDetailsChanged
                        .referenceForDefendant(claim.getReferenceNumber(), "claimant")
        );
    }

    private Map<String, String> aggregateParams(Claim claim, Boolean address, Boolean phone, Boolean email,
                                                Boolean corraddress, Boolean phoneRemoved,
                                                Boolean corrAddressRemoved, Boolean emailRemoved) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(MAIN_ADDRESS_CHANGED, address ? "false" : "true");
        parameters.put(PHONE_NUMBER_CHANGED, phone ? "false" : "true");
        parameters.put(EMAIL_ADDRESS_CHANGED, email ? "false" : "true");
        parameters.put(CORRESPONDENCE_ADDRESS_CHANGED, corraddress ? "false" : "true");
        parameters.put(PHONE_NUMBER_REMOVED, phoneRemoved ? "false" : "true");
        parameters.put(CORRESPONDENCE_ADDRESS_REMOVED, corrAddressRemoved ? "false" : "true");
        parameters.put(EMAIL_ADDRESS_REMOVED, emailRemoved ? "false" : "true");
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        return parameters;
    }
}
