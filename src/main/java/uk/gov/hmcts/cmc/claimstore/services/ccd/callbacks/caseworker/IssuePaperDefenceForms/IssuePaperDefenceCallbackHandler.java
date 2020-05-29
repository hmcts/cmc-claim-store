package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.IssuePaperDefenceForms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_PAPER_DEFENSE_FORMS;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService.DRAFT_LETTER_DOC;

@Service
@ConditionalOnProperty("feature_toggles.ctsc_enabled")
public class IssuePaperDefenceCallbackHandler extends CallbackHandler {
    public static final String CALCULATED_RESPONSE_DEADLINE = "calculatedResponseDeadline";
    public static final String CALCULATED_SERVICE_DATE = "calculatedServiceDate";
    public static final String COVER_LETTER_DOC = "coverLetterDoc";
    public static final String LETTER_NAME = "%s-issue-paper-form.pdf";
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(ISSUE_PAPER_DEFENSE_FORMS);

    private final CaseDetailsConverter caseDetailsConverter;
    private final UserService userService;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final IssueDateCalculator issueDateCalculator;
    private final IssuePaperResponsePostProccessor issuePaperResponsePostProccessor;
    private final IssuePaperResponseLetterService issuePaperResponseLetterService;

    @Autowired
    public IssuePaperDefenceCallbackHandler(
            CaseDetailsConverter caseDetailsConverter,
            UserService userService,
            ResponseDeadlineCalculator responseDeadlineCalculator,
            IssueDateCalculator issueDateCalculator,
            IssuePaperResponsePostProccessor issuePaperResponsePostProccessor,
            IssuePaperResponseLetterService issuePaperResponseLetterService
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.userService = userService;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.issueDateCalculator = issueDateCalculator;
        this.issuePaperResponsePostProccessor = issuePaperResponsePostProccessor;
        this.issuePaperResponseLetterService = issuePaperResponseLetterService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
                CallbackType.ABOUT_TO_START, this::updateDeadlinesAndGenerateDocuments,
                CallbackType.ABOUT_TO_SUBMIT, issuePaperResponsePostProccessor::performPostProcesses
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


    private AboutToStartOrSubmitCallbackResponse updateDeadlinesAndGenerateDocuments(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        var userDetails = userService.getUserDetails(authorisation);
        String caseworkerName = userDetails.getFullName();

        CCDCase updatedCCDCase = addNewDeadlinesToCCDCase(ccdCase);

        CCDRespondent newRespondent = updatedCCDCase.getRespondents().get(0).getValue();
        Map<String, Object> data = Map.of(CALCULATED_RESPONSE_DEADLINE, newRespondent.getResponseDeadline()
                , CALCULATED_SERVICE_DATE, newRespondent.getServedDate());

        CCDDocument coverLetter = issuePaperResponseLetterService.createCoverLetter(ccdCase, caseworkerName, authorisation);
        CCDDocument oconForm = issuePaperResponseLetterService.createOconForm(ccdCase, caseworkerName, claim, authorisation);

        data.put(COVER_LETTER_DOC, coverLetter);
        data.put(DRAFT_LETTER_DOC, oconForm);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data)
                .build();
    }

    private CCDCase addNewDeadlinesToCCDCase(CCDCase ccdCase) {
        LocalDate formIssueDate = issueDateCalculator.calculateIssueDay(LocalDateTime.now());
        LocalDate newServiceDate = responseDeadlineCalculator.calculateServiceDate(formIssueDate);
        LocalDate newResponseDeadline = responseDeadlineCalculator.calculateResponseDeadline(formIssueDate);

        CCDCollectionElement<CCDRespondent> collectionElement = ccdCase.getRespondents().get(0);
        CCDRespondent respondent = collectionElement.getValue().toBuilder()
                .responseDeadline(newResponseDeadline)
                .servedDate(newServiceDate)
                .build();

        return ccdCase.toBuilder()
                .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                        .value(respondent)
                        .id(collectionElement.getId())
                        .build()))
                .build();
    }

}
