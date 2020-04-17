package uk.gov.hmcts.cmc.claimstore.events.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_TYPE;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.RESPONSE_DEADLINE;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Service
public class MoreTimeRequestedCitizenNotificationHandler {

    private static final String REFERENCE_TEMPLATE = "more-time-requested-notification-to-%s-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final GeneralLetterService generalLetterService;
    private final CaseDetailsConverter caseDetailsConverter;

    @Autowired
    public MoreTimeRequestedCitizenNotificationHandler(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        GeneralLetterService generalLetterService,
        CaseDetailsConverter caseDetailsConverter
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.generalLetterService = generalLetterService;
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    public AboutToStartOrSubmitCallbackResponse sendNotifications(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        sendNotificationToClaimant(claim);
        if (claim.getDefendantEmail() != null && claim.getDefendantId() != null) {
            sendEmailNotificationToDefendant(claim);
        } else {
            return createAndPrintLetter();
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void sendEmailNotificationToDefendant(Claim claim) {
            notificationService.sendMail(
                    claim.getDefendantEmail(),
                    notificationsProperties.getTemplates().getEmail().getDefendantMoreTimeRequested(),
                    prepareNotificationParameters(claim),
                    referenceForDefendant(claim.getReferenceNumber())
            );
    }

    private void sendNotificationToClaimant(Claim claim) {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantMoreTimeRequested(),
            prepareNotificationParameters(claim),
            String.format(REFERENCE_TEMPLATE, "claimant", claim.getReferenceNumber())
        );
    }

    private AboutToStartOrSubmitCallbackResponse createAndPrintLetter(CallbackParams callbackParams) {

        return
    }

    private Map<String, String> prepareNotificationParameters(Claim claim) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put(CLAIMANT_TYPE, PartyUtils.getType(claim.getClaimData().getClaimant()));
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(RESPONSE_DEADLINE, formatDate(claim.getResponseDeadline()));
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        return parameters;
    }
}
