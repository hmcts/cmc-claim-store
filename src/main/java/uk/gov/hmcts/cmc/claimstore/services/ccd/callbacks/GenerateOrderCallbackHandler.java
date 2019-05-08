package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.LegalOrderGenerationDeadlinesCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.docassembly.DocAssemblyClient;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyRequest;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.domain.OutputType;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ACTION_REVIEW_COMMENTS;

@Service
@ConditionalOnProperty(prefix = "doc_assembly", name = "url")
public class GenerateOrderCallbackHandler extends CallbackHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${doc_assembly.templateId}")
    private String templateId;

    private final LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator;
    private final DocAssemblyClient docAssemblyClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final JsonMapper jsonMapper;
    private final UserService userService;
    private final DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;

    @Autowired
    public GenerateOrderCallbackHandler(
        UserService userService,
        LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator,
        DocAssemblyClient docAssemblyClient,
        AuthTokenGenerator authTokenGenerator,
        JsonMapper jsonMapper,
        DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper) {
        this.docAssemblyClient = docAssemblyClient;
        this.authTokenGenerator = authTokenGenerator;
        this.jsonMapper = jsonMapper;
        this.userService = userService;
        this.legalOrderGenerationDeadlinesCalculator = legalOrderGenerationDeadlinesCalculator;
        this.docAssemblyTemplateBodyMapper = docAssemblyTemplateBodyMapper;
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
        return Arrays.asList(CaseEvent.GENERATE_ORDER, ACTION_REVIEW_COMMENTS);
    }

    private CallbackResponse prepopulateOrder(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = jsonMapper.fromMap(
            callbackRequest.getCaseDetails().getData(), CCDCase.class);
        LocalDate deadline = legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines();
        Map<String, Object> data = new HashMap<>();
        data.put("directionList", ImmutableList.of(
            CCDOrderDirectionType.DOCUMENTS.name(),
            CCDOrderDirectionType.EYEWITNESS.name()
        ));
        data.put("docUploadDeadline", deadline);
        data.put("eyewitnessUploadDeadline", deadline);
        data.put("preferredCourt", ccdCase.getPreferredCourt());
        addCourtData(ccdCase, data);
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(data)
            .build();
    }

    private CallbackResponse generateOrder(CallbackParams callbackParams) {
        logger.info("Generate order callback: creating order document");
        CallbackRequest callbackRequest = callbackParams.getRequest();
        CCDCase ccdCase = jsonMapper.fromMap(
            callbackRequest.getCaseDetails().getData(), CCDCase.class);

        String authorisation = callbackParams.getParams()
            .get(CallbackParams.Params.BEARER_TOKEN).toString();

        logger.info("Generate order callback: creating request for doc assembly");

        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(templateId)
            .outputType(OutputType.DOC)
            .formPayload(docAssemblyTemplateBodyMapper.from(
                ccdCase, ccdCase.getOrderGenerationData(), userService.getUserDetails(authorisation)))
            .build();

        logger.info("Generate order callback: sending request to doc assembly");

        DocAssemblyResponse docAssemblyResponse = docAssemblyClient.generateOrder(
            authorisation,
            authTokenGenerator.generate(),
            docAssemblyRequest
        );

        logger.info("Generate order callback: received response from doc assembly");

        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(ImmutableMap.of(
                "draftOrderDoc",
                CCDDocument.builder()
                    .documentUrl(docAssemblyResponse.getRenditionOutputLocation())
                    .build()
            ))
            .build();
    }

    private void addCourtData(CCDCase ccdCase, Map<String, Object> data) {
        String newRequestedCourt = null;
        String preferredCourtObjectingParty = null;
        String preferredCourtObjectingReason = null;
        CCDApplicant applicant = ccdCase.getApplicants().get(0).getValue();
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();

        if (StringUtils.isNotBlank(applicant.getPreferredCourtReason())) {
            newRequestedCourt = applicant.getPreferredCourtName();
            preferredCourtObjectingParty = "CLAIMANT";
            preferredCourtObjectingReason = applicant.getPreferredCourtReason();
        } else if (StringUtils.isNotBlank(respondent.getPreferredCourtReason())) {
            newRequestedCourt = respondent.getPreferredCourtName();
            preferredCourtObjectingParty = "DEFENDANT";
            preferredCourtObjectingReason = respondent.getPreferredCourtReason();
        }

        data.put("newRequestedCourt", newRequestedCourt);
        data.put("preferredCourtObjectingParty", preferredCourtObjectingParty);
        data.put("preferredCourtObjectingReason", preferredCourtObjectingReason);
    }
}
