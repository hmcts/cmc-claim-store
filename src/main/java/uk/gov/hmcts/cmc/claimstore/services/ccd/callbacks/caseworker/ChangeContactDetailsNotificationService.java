package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.claimstore.config.LoggerHandler;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ContactDetailsChanged;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Service
public class ChangeContactDetailsNotificationService {

    private static final String ERROR_MESSAGE =
        "There was a technical problem. Nothing has been sent. You need to try again.";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final CaseDetailsConverter caseDetailsConverter;
    private static final Logger logger = LoggerFactory.getLogger(LoggerHandler.class);

    @Autowired
    public ChangeContactDetailsNotificationService(
        CaseDetailsConverter caseDetailsConverter,
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.caseDetailsConverter = caseDetailsConverter;
    }

    public CallbackResponse sendEmailToRightRecipient(CCDCase ccdCase, Claim claim) {
        try {

            EmailTemplates templates = notificationsProperties.getTemplates().getEmail();
            if (ccdCase.getContactChangeParty() == CCDContactPartyType.CLAIMANT) {
                notifyParty(claim, claim.getDefendantEmail(),
                    templates.getClaimantContactDetailsChanged(), "defendant");
            } else {
                notifyParty(claim, claim.getSubmitterEmail(),
                    templates.getDefendantContactDetailsChanged(), "claimant");
            }
        } catch (Exception e) {
            logger.info("Sending email to party failed", e);
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(Collections.singletonList(ERROR_MESSAGE))
                .build();
        }

        CCDCase updatedCase = ccdCase.toBuilder()
            .contactChangeParty(null)
            .contactChangeContent(null)
            .generalLetterContent(null)
            .build();

        logger.info("Change Contact Details: Email was sent, case is being updated");
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDetailsConverter.convertToMap(updatedCase))
            .build();
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
