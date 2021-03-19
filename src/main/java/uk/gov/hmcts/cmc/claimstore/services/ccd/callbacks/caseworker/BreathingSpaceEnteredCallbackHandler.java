package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.DocumentPublishService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_PAPER_DEFENSE_FORMS;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildBreathingSpaceEnteredFileBaseName;

@Service
public class BreathingSpaceEnteredCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(ISSUE_PAPER_DEFENSE_FORMS);

    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final DocumentPublishService documentPublishService;
    private final String breathingSpaceEnteredTemplateID;
    private final BreathingSpaceLetterService breathingSpaceLetterService;

    @Autowired
    public BreathingSpaceEnteredCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        DocumentPublishService documentPublishService,
        @Value("${doc_assembly.breathingSpaceEnteredTemplateID}") String breathingSpaceEnteredTemplateID,
        BreathingSpaceLetterService breathingSpaceLetterService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.documentPublishService = documentPublishService;
        this.breathingSpaceEnteredTemplateID = breathingSpaceEnteredTemplateID;
        this.breathingSpaceLetterService = breathingSpaceLetterService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_SUBMIT, this::performPostProcesses
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private CallbackResponse performPostProcesses(CallbackParams callbackParams) {

        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        sendNotificationToClaimant(claim);
        CCDCase updatedCase = ccdCase;
        if (isDefendentLinked(claim)) {
            sendEmailNotificationToDefendant(claim);
        } else {
            String letter = breathingSpaceLetterService.createGeneralLetter(ccdCase, authorisation,
                breathingSpaceEnteredTemplateID);

            CCDDocument letterDoc = CCDDocument.builder().documentUrl(letter)
                .documentFileName(buildBreathingSpaceEnteredFileBaseName(ccdCase.getPreviousServiceCaseReference()))
                .build();

            updatedCase = breathingSpaceLetterService.publishLetter(ccdCase, claim, authorisation, letterDoc);
        }

        var builder = AboutToStartOrSubmitCallbackResponse.builder();
        return builder.data(caseDetailsConverter.convertToMap(updatedCase)).build();
    }

    private void sendEmailNotificationToDefendant(Claim claim) {
        notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getBreathingSpaceEmailToDefendant(),
            prepareNotificationParameters(claim),
            referenceForDefendant(claim.getReferenceNumber())
        );
    }

    private void sendNotificationToClaimant(Claim claim) {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getBreathingSpaceEmailToClaimant(),
            prepareNotificationParameters(claim),
            referenceForClaimant(claim.getReferenceNumber())
        );
    }

    private Map<String, String> prepareNotificationParameters(Claim claim) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber(),
            CLAIMANT_NAME, claim.getClaimData().getClaimant().getName(),
            DEFENDANT_NAME, claim.getClaimData().getDefendant().getName()
        );
    }

    private boolean isDefendentLinked(Claim claim) {
        return !StringUtils.isBlank(claim.getDefendantId());
    }

}
