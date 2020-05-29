package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

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
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.GeneralLetterContent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_PAPER_DEFENSE_FORMS;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService.DRAFT_LETTER_DOC;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.RESPONSE_DEADLINE;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Service
@ConditionalOnProperty("feature_toggles.ctsc_enabled")
public class IssuePaperDefenceCallbackHandler extends CallbackHandler {
    public static final String CALCULATED_RESPONSE_DEADLINE = "calculatedResponseDeadline";
    public static final String CALCULATED_SERVICE_DATE = "calculatedServiceDate";
    //to be added when design is complete
    public static final String STATIC_LETTER_CONTENT = "";
    public static final String COVER_LETTER_DOC = "coverLetterDoc";
    public static final String LETTER_NAME = "%s-issue-paper-form.pdf";
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(ISSUE_PAPER_DEFENSE_FORMS);

    private static final String ERROR_MESSAGE =
            "There was a technical problem. Nothing has been sent. You need to try again.";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final UserService userService;
    private final GeneralLetterService generalLetterService;
    private final String generalLetterTemplateId;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final IssueDateCalculator issueDateCalculator;
    private final EventProducer eventProducer;
    private final String oconFormTemplateId;
    private final DocAssemblyService docAssemblyService;

    @Autowired
    public IssuePaperDefenceCallbackHandler(
            CaseDetailsConverter caseDetailsConverter,
            NotificationService notificationService,
            NotificationsProperties notificationsProperties,
            GeneralLetterService generalLetterService,
            UserService userService,
            ResponseDeadlineCalculator responseDeadlineCalculator,
            IssueDateCalculator issueDateCalculator,
            EventProducer eventProducer,
            DocAssemblyService docAssemblyService,
            @Value("${doc_assembly.generalLetterTemplateId}") String generalLetterTemplateId,
            @Value("${doc_assembly.oconFormTemplateId}") String oconFormTemplateId
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.generalLetterService = generalLetterService;
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.userService = userService;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.issueDateCalculator = issueDateCalculator;
        this.generalLetterTemplateId = generalLetterTemplateId;
        this.eventProducer = eventProducer;
        this.oconFormTemplateId = oconFormTemplateId;
        this.docAssemblyService = docAssemblyService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
                CallbackType.ABOUT_TO_START, this::calculateDeadlines,
                CallbackType.MID, this::createAndSendPaperDefenceForms,
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


    private AboutToStartOrSubmitCallbackResponse calculateDeadlines(CallbackParams callbackParams) {
        LocalDate formIssueDate = issueDateCalculator.calculateIssueDay(LocalDateTime.now());
        LocalDate newServiceDate = responseDeadlineCalculator.calculateServiceDate(formIssueDate);
        LocalDate newResponseDeadline = responseDeadlineCalculator.calculateResponseDeadline(formIssueDate);

        Map<String, Object> data = Map.of(CALCULATED_RESPONSE_DEADLINE, newResponseDeadline
                , CALCULATED_SERVICE_DATE, newServiceDate);

        return AboutToStartOrSubmitCallbackResponse.builder().data(data).build();
    }

    private AboutToStartOrSubmitCallbackResponse createAndSendPaperDefenceForms(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();

        var response = AboutToStartOrSubmitCallbackResponse.builder();
        LocalDate deadline = ccdCase.getCalculatedResponseDeadline();

        String content = String.format(STATIC_LETTER_CONTENT, getClaimantName(ccdCase), formatDate(deadline));
        CCDCase updated = setCoverLetterContent(ccdCase, content, authorisation);
        String CoverLetterUrl = generalLetterService.generateLetter(updated, authorisation, generalLetterTemplateId);

        CCDDocument oconForm = docAssemblyService.generateDocument(authorisation,
                formPayloadForCourt,
                oconFormTemplateId);

        Map<String, Object> data = Map.of(COVER_LETTER_DOC, CCDDocument.builder().documentUrl(CoverLetterUrl).build(),
                DRAFT_LETTER_DOC, oconForm);

        return response
                .data(data)
                .build();
    }

    private CallbackResponse performPostProcesses(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);

        LocalDate responseDeadline = ccdCase.getCalculatedResponseDeadline();
        LocalDate serviceDate = ccdCase.getCalculatedServiceDate();
        //ccd case does not have service date, is this enough?
        CCDCase updatedCase = addDeadlineToCCDCase(ccdCase, responseDeadline);
        Claim updatedClaim = claim.toBuilder()
                .responseDeadline(responseDeadline)
                .serviceDate(serviceDate)
                .build();

        var builder = AboutToStartOrSubmitCallbackResponse.builder();
        try {
            notifyClaimant(updatedClaim);
            String filename = String.format(LETTER_NAME, updatedClaim.getReferenceNumber());
            eventProducer.createPaperDefenceEvent(updatedClaim.getClaimDocumentCollection().get().getDocument());
            updatedCase = generalLetterService.publishLetter(updatedCase, updatedClaim, authorisation, filename);
            } catch (Exception e) {
            logger.error("Error notifying citizens", e);
            return builder.errors(Collections.singletonList(ERROR_MESSAGE)).build();
        }
        return builder.data(caseDetailsConverter.convertToMap(updatedCase)).build();
    }

    private CCDCase addDeadlineToCCDCase(CCDCase ccdCase, LocalDate newDeadline) {
        CCDCollectionElement<CCDRespondent> collectionElement = ccdCase.getRespondents().get(0);
        CCDRespondent respondent = collectionElement.getValue().toBuilder()
                .responseDeadline(newDeadline)
                .build();
        return ccdCase.toBuilder()
                .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                        .value(respondent)
                        .id(collectionElement.getId())
                        .build()))
                .calculatedResponseDeadline(null)
                .build();
    }

    private void notifyClaimant(Claim claim) {
        notificationService.sendMail(
                claim.getSubmitterEmail(),
                notificationsProperties.getTemplates().getEmail().getDefendantAskedToRespondByPost(),
                aggregateParams(claim),
                NotificationReferenceBuilder.IssuePaperDefence
                        .notifyClaimantPaperResponseFormsSentToDefendant(claim.getReferenceNumber(), "claimant")
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(RESPONSE_DEADLINE, formatDate(claim.getResponseDeadline()));
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        return parameters;
    }

    private CCDCase setCoverLetterContent(CCDCase ccdCase, String content, String authorisation) {
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
}
