package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
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

@Service
@ConditionalOnProperty("feature_toggles.ctsc_enabled")
public class IssuePaperDefenceCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(ISSUE_PAPER_DEFENSE_FORMS);
    private static final Logger logger = LoggerFactory.getLogger(IssuePaperDefenceCallbackHandler.class);
    private static final String ERROR_MESSAGE =
            "There was a technical problem. Nothing has been sent. You need to try again.";

    private final CaseDetailsConverter caseDetailsConverter;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final IssueDateCalculator issueDateCalculator;
    private final IssuePaperResponseLetterService issuePaperResponseLetterService;
    private final EventProducer eventProducer;
    private final IssuePaperResponseNotificationService issuePaperResponseNotificationService;

    @Autowired
    public IssuePaperDefenceCallbackHandler(
            CaseDetailsConverter caseDetailsConverter,
            EventProducer eventProducer,
            ResponseDeadlineCalculator responseDeadlineCalculator,
            IssueDateCalculator issueDateCalculator,
            IssuePaperResponseLetterService issuePaperResponseLetterService,
            IssuePaperResponseNotificationService issuePaperResponseNotificationService
    ) {
        this.eventProducer = eventProducer;
        this.caseDetailsConverter = caseDetailsConverter;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.issueDateCalculator = issueDateCalculator;
        this.issuePaperResponseLetterService = issuePaperResponseLetterService;
        this.issuePaperResponseNotificationService = issuePaperResponseNotificationService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
                CallbackType.ABOUT_TO_SUBMIT, this::issuePaperDefence
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


    private AboutToStartOrSubmitCallbackResponse issuePaperDefence(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();

        CCDCase updatedCCDCase = addNewDeadlinesToCCDCase(ccdCase);

        Claim updatedClaim = claim.toBuilder()
                .responseDeadline(respondent.getResponseDeadline())
                .serviceDate(respondent.getServedDate())
                .build();

        CCDDocument coverLetter = issuePaperResponseLetterService.createCoverLetter(updatedCCDCase, authorisation);
        CCDDocument oconForm = issuePaperResponseLetterService.createOconForm(updatedCCDCase, claim, authorisation);

        var builder = AboutToStartOrSubmitCallbackResponse.builder();
        try {
            issuePaperResponseNotificationService.notifyClaimant(updatedClaim);
            eventProducer.createPaperDefenceEvent(updatedClaim, oconForm, coverLetter);
            CCDCase ccdCaseWithDocs = issuePaperResponseLetterService
                    .updateCaseDocumentsWithDefendantLetter(updatedCCDCase, claim, coverLetter);
            return builder.data(caseDetailsConverter.convertToMap(ccdCaseWithDocs)).build();
        } catch (Exception e) {
            logger.error("Error notifying citizens", e);
            return builder.errors(Collections.singletonList(ERROR_MESSAGE)).build();

        }
    }

    private CCDCase addNewDeadlinesToCCDCase(CCDCase ccdCase) {
        LocalDate formIssueDate = issueDateCalculator.calculateIssueDay(LocalDateTime.now());
        LocalDate newServiceDate = responseDeadlineCalculator.calculateServiceDate(formIssueDate);
        LocalDate newResponseDeadline = responseDeadlineCalculator.calculateResponseDeadline(formIssueDate);
        LocalDate newExtendedResponseDeadline =
                responseDeadlineCalculator.calculatePostponedResponseDeadline(formIssueDate);

        CCDCollectionElement<CCDRespondent> collectionElement = ccdCase.getRespondents().get(0);
        CCDRespondent respondent = collectionElement.getValue().toBuilder()
                .responseDeadline(newResponseDeadline)
                .servedDate(newServiceDate)
                .extendedResponseDeadline(newExtendedResponseDeadline)
                .build();
        return ccdCase.toBuilder()
                .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                        .value(respondent)
                        .id(collectionElement.getId())
                        .build()))
                .build();
    }

}
