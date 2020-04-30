package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RESPONSE_MORE_TIME;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_TYPE;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.RESPONSE_DEADLINE;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Service
@ConditionalOnProperty("feature_toggles.ctsc_enabled")
public class MoreTimeRequestedCallbackHandler extends CallbackHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);

    private static final List<CaseEvent> EVENTS = Collections.singletonList(RESPONSE_MORE_TIME);

    private final EventProducer eventProducer;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final GeneralLetterService generalLetterService;

    private static final String PREVIEW_SENTENCE = "The response deadline will be %s .";
    private static final String ERROR_MESSAGE =
        "There was a technical problem. Nothing has been sent. You need to try again.";
    private static final String STANDARD_DEADLINE_TEXT = String.join("%n",
        "You’ve been given an extra 14 days to respond to the claim made against you by %s.",
        "",
        "You now need to respond to the claim before 4pm on %s.",
        "",
        "If you don’t respond, you could get a County Court Judgment (CCJ). This may make it harder to get "
            + "credit, such as a mobile phone contract, credit card or mortgage.");
    private static final String RESPONSE_DEADLINE_PREVIEW = "responseDeadlinePreview";

    @Autowired
    public MoreTimeRequestedCallbackHandler(
        EventProducer eventProducer,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        MoreTimeRequestRule moreTimeRequestRule,
        CaseDetailsConverter caseDetailsConverter,
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        GeneralLetterService generalLetterService) {

        this.eventProducer = eventProducer;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.moreTimeRequestRule = moreTimeRequestRule;
        this.caseDetailsConverter = caseDetailsConverter;
        this.generalLetterService = generalLetterService;
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_START, this::requestMoreTimeViaCaseworker,
            CallbackType.ABOUT_TO_SUBMIT, this::sendNotifications
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

    public AboutToStartOrSubmitCallbackResponse requestMoreTimeViaCaseworker(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());

        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());

        List<String> validationResult = this.moreTimeRequestRule.validateMoreTimeCanBeRequested(claim);
        var builder = AboutToStartOrSubmitCallbackResponse
            .builder();
        if (!validationResult.isEmpty()) {
            return builder
                .errors(validationResult)
                .build();
        }
        Map<String, Object> data = new HashMap<>(caseDetailsConverter.convertToMap(ccdCase));
        data.put(RESPONSE_DEADLINE_PREVIEW, String.format(PREVIEW_SENTENCE, formatDate(newDeadline)));
        return builder
            .data(data)
            .build();
    }

    public CallbackResponse sendNotifications(CallbackParams callbackParams) {
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails());

        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());

        CCDCase newDeadlineCcdCase = addDeadlineToCCDCase(ccdCase, newDeadline);
        Claim newDeadlineClaim = claim.toBuilder().responseDeadline(newDeadline).build();

        try {
            eventProducer.createMoreTimeForResponseRequestedEvent(
                newDeadlineClaim,
                newDeadlineClaim.getResponseDeadline(),
                newDeadlineClaim.getClaimData().getDefendant().getEmail().orElse(null)
            );

            sendNotificationToClaimant(newDeadlineClaim);

            if (newDeadlineClaim.getDefendantEmail() != null && newDeadlineClaim.getDefendantId() != null) {
                sendEmailNotificationToDefendant(newDeadlineClaim);
                return AboutToStartOrSubmitCallbackResponse.builder().build();

            } else {
                String content = String.format(STANDARD_DEADLINE_TEXT,
                    newDeadlineClaim.getClaimData().getClaimant().getName(),
                    formatDate(newDeadlineClaim.getResponseDeadline()));
                String filename = String.format("%s-response-deadline-extended.pdf",
                    newDeadlineClaim.getReferenceNumber());
                CCDCase updatedCCDCase = generalLetterService.createAndPrintLetter(newDeadlineCcdCase,
                    newDeadlineClaim, authorisation, content, filename, CCDContactPartyType.DEFENDANT);

                return AboutToStartOrSubmitCallbackResponse
                        .builder()
                        .data(caseDetailsConverter.convertToMap(updateMoreTimeNeededOption(updatedCCDCase)))
                        .build();
            }

        } catch (Exception e) {
            logger.error("Error notifying citizens", e);
            return AboutToStartOrSubmitCallbackResponse
                .builder()
                .errors(Collections.singletonList(ERROR_MESSAGE))
                .build();
        }
    }

    private CCDCase addDeadlineToCCDCase(CCDCase ccdCase, LocalDate newDeadline) {
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue().toBuilder()
            .responseDeadline(newDeadline)
            .build();
        return ccdCase.toBuilder()
            .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(respondent)
                .build()))
            .build();
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
            referenceForClaimant(claim.getReferenceNumber())
        );
    }

    private Map<String, String> prepareNotificationParameters(Claim claim) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber(),
            CLAIMANT_TYPE, PartyUtils.getType(claim.getClaimData().getClaimant()),
            CLAIMANT_NAME, claim.getClaimData().getClaimant().getName(),
            DEFENDANT_NAME, claim.getClaimData().getDefendant().getName(),
            RESPONSE_DEADLINE, formatDate(claim.getResponseDeadline()),
            FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl()
        );
    }

    private CCDCase updateMoreTimeNeededOption(CCDCase ccdCase) {
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue().toBuilder()
            .responseMoreTimeNeededOption(CCDYesNoOption.YES)
            .build();
        CCDCase updatedCCdCase = ccdCase.toBuilder()
            .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(respondent)
                .build()))
            .build();
        return updatedCCdCase;
    }
}
