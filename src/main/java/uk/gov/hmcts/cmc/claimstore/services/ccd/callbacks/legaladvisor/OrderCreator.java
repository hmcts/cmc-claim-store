package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDResponseSubjectType;
import uk.gov.hmcts.cmc.claimstore.services.LegalOrderGenerationDeadlinesCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules.GenerateOrderRule;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.claimstore.utils.DirectionsQuestionnaireUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType.BOTH;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.DOCUMENTS;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.EYEWITNESS;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class OrderCreator {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String DOC_UPLOAD_DEADLINE = "docUploadDeadline";
    private static final String EYEWITNESS_UPLOAD_DEADLINE = "eyewitnessUploadDeadline";
    private static final String DOC_UPLOAD_FOR_PARTY = "docUploadForParty";
    private static final String EYEWITNESS_UPLOAD_FOR_PARTY = "eyewitnessUploadForParty";
    private static final String PAPER_DETERMINATION = "paperDetermination";
    private static final String DRAFT_ORDER_DOC = "draftOrderDoc";
    private static final String NEW_REQUESTED_COURT = "newRequestedCourt";
    private static final String PREFERRED_COURT_OBJECTING_PARTY = "preferredCourtObjectingParty";
    private static final String PREFERRED_COURT_OBJECTING_REASON = "preferredCourtObjectingReason";
    private static final String DIRECTION_LIST = "directionList";
    private static final String PREFERRED_DQ_COURT = "preferredDQCourt";
    private static final String EXPERT_PERMISSION_BY_CLAIMANT = "expertReportPermissionPartyAskedByClaimant";
    private static final String EXPERT_PERMISSION_BY_DEFENDANT = "expertReportPermissionPartyAskedByDefendant";
    private static final String GRANT_EXPERT_REPORT_PERMISSION = "grantExpertReportPermission";

    private final LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DocAssemblyService docAssemblyService;
    private final GenerateOrderRule generateOrderRule;
    private final boolean jddoEnabled;

    public OrderCreator(
        LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator,
        CaseDetailsConverter caseDetailsConverter,
        DocAssemblyService docAssemblyService,
        GenerateOrderRule generateOrderRule,
        @Value("${feature_toggles.jddo:false}") boolean jddoEnabled
    ) {
        this.legalOrderGenerationDeadlinesCalculator = legalOrderGenerationDeadlinesCalculator;
        this.caseDetailsConverter = caseDetailsConverter;
        this.docAssemblyService = docAssemblyService;
        this.generateOrderRule = generateOrderRule;
        this.jddoEnabled = jddoEnabled;
    }

    public CallbackResponse prepopulateOrder(CallbackParams callbackParams) {
        logger.info("Order creator: pre populating order fields");
        CallbackRequest callbackRequest = callbackParams.getRequest();

        Map<String, Object> data = new HashMap<>();
        data.put(DIRECTION_LIST, ImmutableList.of(DOCUMENTS.name(), EYEWITNESS.name()));

        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());
        addCourtData(claim, ccdCase, data);

        LocalDate deadline = legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines();
        data.put(DOC_UPLOAD_DEADLINE, deadline);
        data.put(EYEWITNESS_UPLOAD_DEADLINE, deadline);
        data.put(DOC_UPLOAD_FOR_PARTY, BOTH.name());
        data.put(EYEWITNESS_UPLOAD_FOR_PARTY, BOTH.name());
        data.put(PAPER_DETERMINATION, NO.name());

        if (jddoEnabled) {
            data.put(GRANT_EXPERT_REPORT_PERMISSION, NO);
        }

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(data)
            .build();
    }

    public CallbackResponse generateOrder(CallbackParams callbackParams) {
        logger.info("Order creator: creating order document");
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());

        List<String> validations = generateOrderRule.validateExpectedFieldsAreSelectedByLegalAdvisor(ccdCase);
        if (!validations.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(validations).build();
        }

        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        DocAssemblyResponse docAssemblyResponse = docAssemblyService.createOrder(ccdCase, authorisation);
        logger.info("Order creator: received response from doc assembly");

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(ImmutableMap.of(
                DRAFT_ORDER_DOC,
                CCDDocument.builder().documentUrl(docAssemblyResponse.getRenditionOutputLocation()).build()
            ))
            .build();
    }

    private void addCourtData(Claim claim, CCDCase ccdCase, Map<String, Object> data) {
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();

        CCDResponseRejection claimantResponse = Optional.ofNullable(respondent.getClaimantResponse())
            .map(CCDResponseRejection.class::cast)
            .orElseThrow(() -> new IllegalStateException("Claimant Response not present"));

        CCDDirectionsQuestionnaire claimantDQ = claimantResponse.getDirectionsQuestionnaire();
        CCDDirectionsQuestionnaire defendantDQ = respondent.getDirectionsQuestionnaire();

        String newRequestedCourt = null;
        String preferredCourtObjectingParty = null;
        String preferredCourtObjectingReason = null;

        if (claimantDQ != null && StringUtils.isNotBlank(claimantDQ.getExceptionalCircumstancesReason())) {
            newRequestedCourt = claimantDQ.getHearingLocation();
            preferredCourtObjectingParty = CCDResponseSubjectType.RES_CLAIMANT.getValue();
            preferredCourtObjectingReason = claimantDQ.getExceptionalCircumstancesReason();

        } else if (defendantDQ != null && StringUtils.isNotBlank(defendantDQ.getExceptionalCircumstancesReason())) {
            newRequestedCourt = defendantDQ.getHearingLocation();
            preferredCourtObjectingParty = CCDResponseSubjectType.RES_DEFENDANT.getValue();
            preferredCourtObjectingReason = defendantDQ.getExceptionalCircumstancesReason();
        }

        data.put(NEW_REQUESTED_COURT, newRequestedCourt);
        data.put(PREFERRED_COURT_OBJECTING_PARTY, preferredCourtObjectingParty);
        data.put(PREFERRED_COURT_OBJECTING_REASON, preferredCourtObjectingReason);
        data.put(PREFERRED_DQ_COURT, DirectionsQuestionnaireUtils.getPreferredCourt(claim));

        if (Optional.ofNullable(claimantDQ).isPresent()) {
            data.put(EXPERT_PERMISSION_BY_CLAIMANT, hasRequestedExpertPermission(claimantDQ));
        }

        if (Optional.ofNullable(defendantDQ).isPresent()) {
            data.put(EXPERT_PERMISSION_BY_DEFENDANT, hasRequestedExpertPermission(defendantDQ));
        }
    }

    private CCDYesNoOption hasRequestedExpertPermission(CCDDirectionsQuestionnaire directionsQuestionnaire) {
        return directionsQuestionnaire.getExpertRequired() != null
            && directionsQuestionnaire.getExpertRequired().toBoolean()
            && (hasRequestedForPermissionWithProvidedEvidence(directionsQuestionnaire)
            || hasProvidedExpertReports(directionsQuestionnaire))
            ? YES
            : NO;
    }

    private boolean hasProvidedExpertReports(CCDDirectionsQuestionnaire directionsQuestionnaire) {
        return directionsQuestionnaire.getExpertReports() != null
            && !directionsQuestionnaire.getExpertReports().isEmpty();
    }

    private boolean hasRequestedForPermissionWithProvidedEvidence(CCDDirectionsQuestionnaire directionsQuestionnaire) {
        return directionsQuestionnaire.getPermissionForExpert() != null
            && directionsQuestionnaire.getPermissionForExpert().toBoolean()
            && StringUtils.isNotBlank(directionsQuestionnaire.getExpertEvidenceToExamine());
    }
}
