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
import uk.gov.hmcts.cmc.claimstore.rules.ClaimDeadlineService;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_PAPER_DEFENSE_FORMS;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Service
@ConditionalOnProperty("feature_toggles.ctsc_enabled")
public class IssuePaperDefenceCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(ISSUE_PAPER_DEFENSE_FORMS);
    private static final Logger logger = LoggerFactory.getLogger(IssuePaperDefenceCallbackHandler.class);
    private static final String ERROR_MESSAGE =
        "There was a technical problem. Nothing has been sent. You need to try again.";
    private static final String CLAIMANT_ISSUED_CCJ =
        "OCON9x form cannot be sent out as CCJ already issued by claimant.";

    private final CaseDetailsConverter caseDetailsConverter;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final IssueDateCalculator issueDateCalculator;
    private final IssuePaperResponseNotificationService issuePaperResponseNotificationService;
    private final DocumentPublishService documentPublishService;
    private final LaunchDarklyClient launchDarklyClient;
    private final ClaimDeadlineService claimDeadlineService;

    @Autowired
    public IssuePaperDefenceCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        IssueDateCalculator issueDateCalculator,
        IssuePaperResponseNotificationService issuePaperResponseNotificationService,
        DocumentPublishService documentPublishService,
        LaunchDarklyClient launchDarklyClient,
        ClaimDeadlineService claimDeadlineService
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.issueDateCalculator = issueDateCalculator;
        this.issuePaperResponseNotificationService = issuePaperResponseNotificationService;
        this.documentPublishService = documentPublishService;
        this.launchDarklyClient = launchDarklyClient;
        this.claimDeadlineService = claimDeadlineService;
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
        var caseDetails = callbackParams.getRequest().getCaseDetails();
        var ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        var ccdRespondent = ccdCase.getRespondents().get(0).getValue();
        LocalDate paperFormServedDate;
        LocalDate responseDeadline;
        LocalDate extendedResponseDeadline;
        LocalDate paperFormIssueDate;
        LocalDate existingDeadline =
            responseDeadlineCalculator.calculateResponseDeadline(ccdCase.getIssuedOn());
        boolean featureEnabled = launchDarklyClient.isFeatureEnabled("ocon-enhancement-2",
            LaunchDarklyClient.CLAIM_STORE_USER);
        var isPastDeadline = false;
        var disableN9Form = false;
        if (featureEnabled) {
            isPastDeadline = claimDeadlineService.isPastDeadline(nowInLocalZone(), existingDeadline);
            disableN9Form = disableN9FormFromOCON9x(ccdRespondent, isPastDeadline);
        }
        if (!ccdRespondent.isOconFormSent()) {
            paperFormIssueDate = issueDateCalculator.calculateIssueDay(LocalDateTime.now());
            paperFormServedDate = responseDeadlineCalculator.calculateServiceDate(paperFormIssueDate);
            if (featureEnabled) {
                responseDeadline = getResponseDeadline(ccdRespondent, paperFormIssueDate, ccdCase, isPastDeadline);
                extendedResponseDeadline = getExtendedResponseDeadline(ccdCase, paperFormIssueDate, isPastDeadline);
            } else {
                if (CCDYesNoOption.YES.equals(ccdRespondent.getResponseMoreTimeNeededOption())) {
                    responseDeadline =
                        responseDeadlineCalculator.calculatePostponedResponseDeadline(paperFormIssueDate);
                } else {
                    responseDeadline = responseDeadlineCalculator.calculateResponseDeadline(paperFormIssueDate);
                }
                extendedResponseDeadline =
                    responseDeadlineCalculator.calculatePostponedResponseDeadline(paperFormIssueDate);
            }
        } else {
            paperFormServedDate = ccdRespondent.getPaperFormServedDate();
            responseDeadline = ccdRespondent.getResponseDeadline();
            extendedResponseDeadline = ccdCase.getExtendedResponseDeadline();
            paperFormIssueDate = ccdRespondent.getPaperFormIssueDate();
        }
        ccdCase = updateCaseDates(ccdCase, responseDeadline, paperFormServedDate, extendedResponseDeadline,
            paperFormIssueDate);
        var claim = updateClaimDates(caseDetails, responseDeadline);

        var builder = AboutToStartOrSubmitCallbackResponse.builder();
        if (!launchDarklyClient.isFeatureEnabled("ocon-enhancements", LaunchDarklyClient.CLAIM_STORE_USER)
            && ccdRespondent.getCountyCourtJudgmentRequest() != null
            && ccdRespondent.getCountyCourtJudgmentRequest().getType() == CCDCountyCourtJudgmentType.DEFAULT) {
            builder.errors(List.of(CLAIMANT_ISSUED_CCJ));
            return builder.build();
        }
        try {
            var authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
            ccdCase = documentPublishService.publishDocuments(ccdCase, claim, authorisation, extendedResponseDeadline,
                disableN9Form, featureEnabled);
            if (!ccdRespondent.isOconFormSent()) {
                issuePaperResponseNotificationService.notifyClaimant(claim);
            }
            return builder.data(caseDetailsConverter.convertToMap(ccdCase)).build();
        } catch (Exception e) {
            logger.error("Error notifying citizens", e);
            return builder.errors(Collections.singletonList(ERROR_MESSAGE)).build();
        }
    }

    private boolean disableN9FormFromOCON9x(CCDRespondent ccdRespondent, boolean isPastDeadline) {
        var disableN9Form = false;
        if (isPastDeadline || CCDYesNoOption.YES.equals(ccdRespondent.getResponseMoreTimeNeededOption())) {
            disableN9Form = true;
        }
        return disableN9Form;
    }

    private LocalDate getExtendedResponseDeadline(CCDCase ccdCase, LocalDate paperFormIssueDate,
                                                  boolean isPastDeadline) {
        LocalDate extendedResponseDeadline;
        if (isPastDeadline) {
            extendedResponseDeadline =
                responseDeadlineCalculator.calculatePostponedResponseDeadline(ccdCase.getIssuedOn());
        } else {
            extendedResponseDeadline =
                responseDeadlineCalculator.calculatePostponedResponseDeadline(paperFormIssueDate);
        }
        return extendedResponseDeadline;
    }

    private LocalDate getResponseDeadline(CCDRespondent ccdRespondent, LocalDate paperFormIssueDate, CCDCase ccdCase,
                                          boolean isPastDeadline) {

        if (isPastDeadline && CCDYesNoOption.NO.equals(ccdRespondent.getResponseMoreTimeNeededOption())) {
            return responseDeadlineCalculator.calculateResponseDeadline(ccdCase.getIssuedOn());
        } else if (CCDYesNoOption.YES.equals(ccdRespondent.getResponseMoreTimeNeededOption())) {
            return responseDeadlineCalculator.calculatePostponedResponseDeadline(ccdCase.getIssuedOn());
        } else {
            return responseDeadlineCalculator.calculateResponseDeadline(paperFormIssueDate);
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
