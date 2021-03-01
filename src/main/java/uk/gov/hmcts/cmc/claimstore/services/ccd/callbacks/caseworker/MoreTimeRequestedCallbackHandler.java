package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.GeneralLetterContent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimDeadlineService;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
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
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RESPONSE_MORE_TIME;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService.DRAFT_LETTER_DOC;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_TYPE;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.RESPONSE_DEADLINE;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Service
@ConditionalOnProperty("feature_toggles.ctsc_enabled")
public class MoreTimeRequestedCallbackHandler extends CallbackHandler {
    public static final String CALCULATED_RESPONSE_DEADLINE = "calculatedResponseDeadline";
    public static final String LETTER_NAME = "%s-response-deadline-extended.pdf";
    public static final String PREVIEW_SENTENCE = "The response deadline will be %s .";
    public static final String RESPONSE_DEADLINE_PREVIEW = "responseDeadlinePreview";
    public static final String DEADLINE_WARN_MSG = "deadlineWarningMessage";
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(RESPONSE_MORE_TIME);
    private static final String ERROR_MESSAGE =
        "There was a technical problem. Nothing has been sent. You need to try again.";
    private static final String STANDARD_DEADLINE_TEXT = String.join("%n",
        "You’ve been given an extra 14 days to respond to the claim made against you by %s.",
        "",
        "You now need to respond to the claim before 4pm on %s.",
        "",
        "If you don’t respond, you could get a County Court Judgment (CCJ). This may make it harder to get "
            + "credit, such as a mobile phone contract, credit card or mortgage.");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final EventProducer eventProducer;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final UserService userService;
    private final GeneralLetterService generalLetterService;
    private final String generalLetterTemplateId;
    private final LaunchDarklyClient launchDarklyClient;
    private final ClaimDeadlineService claimDeadlineService;

    @Autowired
    public MoreTimeRequestedCallbackHandler(
        EventProducer eventProducer,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        MoreTimeRequestRule moreTimeRequestRule,
        CaseDetailsConverter caseDetailsConverter,
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        GeneralLetterService generalLetterService,
        UserService userService,
        @Value("${doc_assembly.generalLetterTemplateId}") String generalLetterTemplateId,
        LaunchDarklyClient launchDarklyClient,
        ClaimDeadlineService claimDeadlineService
    ) {
        this.eventProducer = eventProducer;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.moreTimeRequestRule = moreTimeRequestRule;
        this.caseDetailsConverter = caseDetailsConverter;
        this.generalLetterService = generalLetterService;
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.userService = userService;
        this.generalLetterTemplateId = generalLetterTemplateId;
        this.launchDarklyClient = launchDarklyClient;
        this.claimDeadlineService = claimDeadlineService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_START, this::calculateResponseDeadline,
            CallbackType.MID, this::generateLetter,
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

    private AboutToStartOrSubmitCallbackResponse calculateResponseDeadline(CallbackParams callbackParams) {
        LocalDate newDeadline;
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
        if (launchDarklyClient.isFeatureEnabled("ocon-enhancement-2", LaunchDarklyClient.CLAIM_STORE_USER)) {
            LocalDate existingDeadline =
                responseDeadlineCalculator.calculateResponseDeadline(ccdCase.getIssuedOn());
            final boolean isPastDeadline = claimDeadlineService.isPastDeadline(nowInLocalZone(), existingDeadline);
            newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(
                respondent.getPaperFormIssueDate() != null && !isPastDeadline ? respondent.getPaperFormIssueDate()
                    : ccdCase.getIssuedOn());
        } else {
            newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(
                respondent.getPaperFormIssueDate() != null ? respondent.getPaperFormIssueDate()
                    : ccdCase.getIssuedOn());
        }
        List<String> validationResult = this.moreTimeRequestRule.validateMoreTimeCanBeRequested(claim, newDeadline);
        var builder = AboutToStartOrSubmitCallbackResponse.builder();
        if (!validationResult.isEmpty()) {
            return builder.errors(validationResult).build();
        }

        String deadlineMessage = "";
        if (launchDarklyClient.isFeatureEnabled("ocon-enhancement-2", LaunchDarklyClient.CLAIM_STORE_USER)) {
            LocalDate existingDeadline =
                responseDeadlineCalculator.calculateResponseDeadline(ccdCase.getIssuedOn());
            final boolean isPastDeadline = claimDeadlineService.isPastDeadline(nowInLocalZone(), existingDeadline);
            if (isPastDeadline) {
                deadlineMessage = "A request for more time must be made within 19 days from claim issue.";
            }
        }

        Map<String, Object> data = Map.of(CALCULATED_RESPONSE_DEADLINE, newDeadline,
            RESPONSE_DEADLINE_PREVIEW, String.format(PREVIEW_SENTENCE, formatDate(newDeadline)), DEADLINE_WARN_MSG,
            deadlineMessage);

        return builder.data(data).build();
    }

    private CallbackResponse generateLetter(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);

        var response = AboutToStartOrSubmitCallbackResponse.builder();

        if (letterNeededForDefendant(ccdCase)) {
            LocalDate deadline = ccdCase.getCalculatedResponseDeadline();
            String content = String.format(STANDARD_DEADLINE_TEXT, getClaimantName(ccdCase), formatDate(deadline));
            String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
            CCDCase updated = setLetterContent(ccdCase, content, authorisation);
            String letterUrl = generalLetterService.generateLetter(updated, authorisation, generalLetterTemplateId);

            return response
                .data(Map.of(DRAFT_LETTER_DOC, CCDDocument.builder().documentUrl(letterUrl).build()))
                .build();
        } else {
            return response.build();
        }

    }

    private CCDCase setLetterContent(CCDCase ccdCase, String content, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        String caseworkerName = userDetails.getFullName();
        GeneralLetterContent generalLetterContent = GeneralLetterContent.builder()
            .caseworkerName(caseworkerName)
            .letterContent(content)
            .issueLetterContact(CCDContactPartyType.DEFENDANT)
            .build();

        return ccdCase.toBuilder()
            .generalLetterContent(generalLetterContent)
            .build();
    }

    private String getClaimantName(CCDCase ccdCase) {
        return ccdCase.getApplicants().get(0).getValue().getPartyName();
    }

    private CallbackResponse performPostProcesses(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        LocalDate responseDeadline = ccdCase.getCalculatedResponseDeadline();
        CCDCase updatedCase = addDeadlineToCCDCase(ccdCase, responseDeadline);
        Claim updatedClaim = claim.toBuilder().responseDeadline(responseDeadline).build();

        var builder = AboutToStartOrSubmitCallbackResponse.builder();
        try {
            eventProducer.createMoreTimeForResponseRequestedEvent(
                updatedClaim,
                updatedClaim.getResponseDeadline(),
                updatedClaim.getClaimData().getDefendant().getEmail().orElse(null)
            );

            sendNotificationToClaimant(updatedClaim);

            if (updatedClaim.getDefendantEmail() != null && updatedClaim.getDefendantId() != null) {
                sendEmailNotificationToDefendant(updatedClaim);
            } else {
                String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
                String filename = String.format(LETTER_NAME, updatedClaim.getReferenceNumber());
                updatedCase = generalLetterService.publishLetter(updatedCase, updatedClaim, authorisation, filename);
            }
            return builder.data(caseDetailsConverter.convertToMap(updatedCase)).build();
        } catch (Exception e) {
            logger.error("Error notifying citizens", e);
            return builder.errors(Collections.singletonList(ERROR_MESSAGE)).build();
        }
    }

    private CCDCase addDeadlineToCCDCase(CCDCase ccdCase, LocalDate newDeadline) {
        CCDCollectionElement<CCDRespondent> collectionElement = ccdCase.getRespondents().get(0);
        CCDRespondent respondent = collectionElement.getValue().toBuilder()
            .responseDeadline(newDeadline)
            .responseMoreTimeNeededOption(CCDYesNoOption.YES)
            .build();
        return ccdCase.toBuilder()
            .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(respondent)
                .id(collectionElement.getId())
                .build()))
            .calculatedResponseDeadline(null)
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

    private boolean letterNeededForDefendant(CCDCase ccdCase) {
        return StringUtils.isBlank(ccdCase.getRespondents().get(0).getValue().getDefendantId());
    }
}
