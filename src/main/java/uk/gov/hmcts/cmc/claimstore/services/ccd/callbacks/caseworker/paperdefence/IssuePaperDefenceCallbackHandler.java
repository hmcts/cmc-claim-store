package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgmentType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
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
    private static final String CLAIMANT_ISSUED_CCJ =
        "CCJ already issued by claimant so OCON9x form cannot be sent out.";

    private final CaseDetailsConverter caseDetailsConverter;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final IssueDateCalculator issueDateCalculator;
    private final IssuePaperResponseNotificationService issuePaperResponseNotificationService;
    private final DocumentPublishService documentPublishService;

    @Autowired
    public IssuePaperDefenceCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        IssueDateCalculator issueDateCalculator,
        IssuePaperResponseNotificationService issuePaperResponseNotificationService,
        DocumentPublishService documentPublishService
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.issueDateCalculator = issueDateCalculator;
        this.issuePaperResponseNotificationService = issuePaperResponseNotificationService;
        this.documentPublishService = documentPublishService;
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
        CCDRespondent ccdRespondent = ccdCase.getRespondents().get(0).getValue();
        LocalDate paperFormServedDate;
        LocalDate responseDeadline;
        LocalDate extendedResponseDeadline;
        LocalDate paperFormIssueDate;
        if (!ccdRespondent.isOconFormSent()) {
            paperFormIssueDate = issueDateCalculator.calculateIssueDay(LocalDateTime.now());
            paperFormServedDate = responseDeadlineCalculator.calculateServiceDate(paperFormIssueDate);
            if (CCDYesNoOption.YES.equals(ccdRespondent.getResponseMoreTimeNeededOption())) {
                responseDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(paperFormIssueDate);
            } else {
                responseDeadline = responseDeadlineCalculator.calculateResponseDeadline(paperFormIssueDate);
            }
            extendedResponseDeadline =
                responseDeadlineCalculator.calculatePostponedResponseDeadline(paperFormIssueDate);
        } else {
            paperFormServedDate = ccdRespondent.getPaperFormServedDate();
            responseDeadline = ccdRespondent.getResponseDeadline();
            extendedResponseDeadline = ccdCase.getExtendedResponseDeadline();
            paperFormIssueDate = ccdRespondent.getPaperFormIssueDate();
        }
        ccdCase = updateCaseDates(ccdCase, responseDeadline, paperFormServedDate, extendedResponseDeadline,
            paperFormIssueDate);
        Claim claim = updateClaimDates(caseDetails, responseDeadline);

        var builder = AboutToStartOrSubmitCallbackResponse.builder();
        if (ccdRespondent.getCountyCourtJudgmentRequest() != null
            && ccdRespondent.getCountyCourtJudgmentRequest().getType() == CCDCountyCourtJudgmentType.DEFAULT) {
            builder.errors(List.of(CLAIMANT_ISSUED_CCJ));
            return builder.build();
        }
        try {
            String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
            ccdCase = documentPublishService.publishDocuments(ccdCase, claim, authorisation, extendedResponseDeadline);
            if (!ccdRespondent.isOconFormSent()) {
                issuePaperResponseNotificationService.notifyClaimant(claim);
            }
            return builder.data(caseDetailsConverter.convertToMap(ccdCase)).build();
        } catch (Exception e) {
            logger.error("Error notifying citizens", e);
            return builder.errors(Collections.singletonList(ERROR_MESSAGE)).build();
        }
    }

    private CCDCase updateCaseDates(
        CCDCase ccdCase,
        LocalDate responseDeadline,
        LocalDate serviceDate,
        LocalDate extendedResponseDeadline,
        LocalDate paperFormIssueDate
    ) {
        CCDCollectionElement<CCDRespondent> collectionElement = ccdCase.getRespondents().get(0);
        CCDRespondent respondent = collectionElement.getValue().toBuilder()
            .responseDeadline(responseDeadline)
            .paperFormServedDate(serviceDate)
            .paperFormIssueDate(paperFormIssueDate)
            .build();
        return ccdCase.toBuilder()
            .extendedResponseDeadline(extendedResponseDeadline)
            .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                .value(respondent)
                .id(collectionElement.getId())
                .build()))
            .build();
    }

    private Claim updateClaimDates(CaseDetails caseDetails, LocalDate responseDeadline) {
        return caseDetailsConverter.extractClaim(caseDetails)
            .toBuilder()
            .responseDeadline(responseDeadline)
            .build();
    }

}
