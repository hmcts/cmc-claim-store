package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.CaseTransferred.referenceForCaseTransferred;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.COURT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.PARTY_NAME;

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
        String partyName = claim.getClaimData().getClaimant().getName();
        notifyParty(claim, claim.getSubmitterEmail(), templates.getCaseTransferred(),
            NotificationReferenceBuilder.CLAIMANT, partyName);
    }

    public void sendClaimUpdatedEmailToDefendant(Claim claim) {

        EmailTemplates templates = notificationsProperties.getTemplates().getEmail();
        String partyName = claim.getClaimData().getDefendant().getName();
        notifyParty(claim, claim.getDefendantEmail(), templates.getCaseTransferred(),
            NotificationReferenceBuilder.DEFENDANT, partyName);
    }

    private void notifyParty(Claim claim, String partyEmail, String emailTemplateId, String party, String partyName) {
        notificationService.sendMail(
            partyEmail,
            emailTemplateId,
            aggregateParams(claim, partyName),
            referenceForCaseTransferred(claim.getReferenceNumber(), party)
        );
    }

    private Map<String, String> aggregateParams(Claim claim, String partyName) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PARTY_NAME, partyName);
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(EXTERNAL_ID, claim.getExternalId());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put(COURT_NAME, claim.getTransferContent().getHearingCourtName());
        return parameters;
    }
}
