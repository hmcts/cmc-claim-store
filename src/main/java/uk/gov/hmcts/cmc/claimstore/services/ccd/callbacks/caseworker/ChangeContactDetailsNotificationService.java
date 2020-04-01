package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.claimstore.config.LoggerHandler;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

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
        boolean errors = false;
        try {

            if (ccdCase.getContactChangeParty() == CCDContactPartyType.CLAIMANT) {
                notifyDefendant(claim);
            } else {
                notifyClaimant(claim);
            }
        } catch (Exception e) {
            logger.info("Sending email to party failed", e);
            errors = true;
        }
        if (!errors) {
            logger.info("Change Contact Details: Email was sent, case is being updated");
            CCDCase updatedCase = ccdCase.toBuilder()
                .contactChangeParty(null)
                .contactChangeContent(null)
                .generalLetterContent(null)
                .build();
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .data(caseDetailsConverter.convertToMap(updatedCase))
                .build();
        } else {
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(Collections.singletonList(ERROR_MESSAGE))
                .build();
        }
    }

    private void notifyClaimant(Claim claim) {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getDefendantContactDetailsChanged(),
            aggregateParams(claim),
            NotificationReferenceBuilder.ContactDetailsChanged
                .referenceForClaimant(claim.getReferenceNumber(), "claimant")
        );
    }

    private void notifyDefendant(Claim claim) {
        notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantContactDetailsChanged(),
            aggregateParams(claim),
            NotificationReferenceBuilder.ContactDetailsChanged
                .referenceForDefendant(claim.getReferenceNumber(), "defendant")
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
