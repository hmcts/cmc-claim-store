package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.legaladvisor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.directionsquestionnaire.CCDDirectionsQuestionnaire;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDResponseSubjectType;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.services.LegalOrderGenerationDeadlinesCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.claimstore.utils.DirectionsQuestionnaireUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.docassembly.DocAssemblyClient;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyRequest;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.domain.OutputType;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ACTION_REVIEW_COMMENTS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDDirectionPartyType.BOTH;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.DOCUMENTS;
import static uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType.EYEWITNESS;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DRAFTED_BY_LEGAL_ADVISOR;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.LEGAL_ADVISOR;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class GenerateOrderCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(LEGAL_ADVISOR);
    private static final List<CaseEvent> EVENTS = ImmutableList.of(GENERATE_ORDER, ACTION_REVIEW_COMMENTS);

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

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${doc_assembly.templateId}")
    private String templateId;

    private final LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator;
    private final DocAssemblyClient docAssemblyClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    private final CaseDetailsConverter caseDetailsConverter;
    private final AppInsights appInsights;

    @Autowired
    public GenerateOrderCallbackHandler(
        UserService userService,
        LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator,
        DocAssemblyClient docAssemblyClient,
        AuthTokenGenerator authTokenGenerator,
        DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper,
        CaseDetailsConverter caseDetailsConverter,
        AppInsights appInsights
    ) {
        this.docAssemblyClient = docAssemblyClient;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.legalOrderGenerationDeadlinesCalculator = legalOrderGenerationDeadlinesCalculator;
        this.docAssemblyTemplateBodyMapper = docAssemblyTemplateBodyMapper;
        this.caseDetailsConverter = caseDetailsConverter;
        this.appInsights = appInsights;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_START, this::prepopulateOrder,
            CallbackType.MID, this::generateOrder
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

    private CallbackResponse prepopulateOrder(CallbackParams callbackParams) {
        logger.info("Generate order callback: prepopulating order fields");
        CallbackRequest callbackRequest = callbackParams.getRequest();

        LocalDate deadline = legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines();
        Map<String, Object> data = new HashMap<>();
        data.put(DIRECTION_LIST, ImmutableList.of(
            DOCUMENTS.name(),
            EYEWITNESS.name()
        ));
        Claim claim = caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());

        addCourtData(claim, ccdCase, data);
        data.put(DOC_UPLOAD_DEADLINE, deadline);
        data.put(EYEWITNESS_UPLOAD_DEADLINE, deadline);
        data.put(DOC_UPLOAD_FOR_PARTY, BOTH.name());
        data.put(EYEWITNESS_UPLOAD_FOR_PARTY, BOTH.name());
        data.put(PAPER_DETERMINATION, NO.name());
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(data)
            .build();
    }

    private CallbackResponse generateOrder(CallbackParams callbackParams) {
        logger.info("Generate order callback: creating order document");
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());

        String authorisation = callbackParams.getParams()
            .get(CallbackParams.Params.BEARER_TOKEN).toString();

        logger.info("Generate order callback: creating request for doc assembly");
        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(templateId)
            .outputType(OutputType.PDF)
            .formPayload(docAssemblyTemplateBodyMapper.from(ccdCase, userService.getUserDetails(authorisation)))
            .build();

        logger.info("Generate order callback: sending request to doc assembly");

        DocAssemblyResponse docAssemblyResponse = docAssemblyClient.generateOrder(
            authorisation,
            authTokenGenerator.generate(),
            docAssemblyRequest
        );

        logger.info("Generate order callback: received response from doc assembly");

        appInsights.trackEvent(DRAFTED_BY_LEGAL_ADVISOR, REFERENCE_NUMBER, ccdCase.getPreviousServiceCaseReference());

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
            .map(r -> (CCDResponseRejection) r)
            .orElseThrow(() -> new IllegalStateException("Claimant Response not present"));

        CCDDirectionsQuestionnaire claimantDQ =
            claimantResponse.getDirectionsQuestionnaire();

        CCDDirectionsQuestionnaire defendantDQ = respondent.getDirectionsQuestionnaire();

        String newRequestedCourt = null;
        String preferredCourtObjectingParty = null;
        String preferredCourtObjectingReason = null;

        if (claimantDQ != null && StringUtils.isNotBlank(claimantDQ.getExceptionalCircumstancesReason())) {
            newRequestedCourt = claimantDQ.getHearingLocation();
            preferredCourtObjectingParty = CCDResponseSubjectType.RES_CLAIMANT.getValue();
            preferredCourtObjectingReason = claimantDQ.getExceptionalCircumstancesReason();
        } else if (defendantDQ != null && StringUtils.isNotBlank(
            defendantDQ.getExceptionalCircumstancesReason())) {
            newRequestedCourt = defendantDQ.getHearingLocation();
            preferredCourtObjectingParty = CCDResponseSubjectType.RES_DEFENDANT.getValue();
            preferredCourtObjectingReason = defendantDQ.getExceptionalCircumstancesReason();
        }

        data.put(NEW_REQUESTED_COURT, newRequestedCourt);
        data.put(PREFERRED_COURT_OBJECTING_PARTY, preferredCourtObjectingParty);
        data.put(PREFERRED_COURT_OBJECTING_REASON, preferredCourtObjectingReason);
        data.put(PREFERRED_DQ_COURT, DirectionsQuestionnaireUtils.getPreferredCourt(claim));
    }
}
