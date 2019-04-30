package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirectionType;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
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

import static java.lang.String.format;

public class GenerateOrderCallbackService {
    @Value("${doc_assembly.templateId}")
    private String templateId;

    private final LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator;
    private final DocAssemblyClient docAssemblyClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final JsonMapper jsonMapper;
    private final UserService userService;
    private final DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;

    public GenerateOrderCallbackService(
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

    public CallbackResponse execute(CallbackType callbackType, CallbackRequest callbackRequest, String authorisation) {
        switch (callbackType) {
            case ABOUT_TO_START:
                return prepopulateFields();
            case MID:
                return createDocument(authorisation, callbackRequest);
            default:
                throw new IllegalArgumentException(
                    format("Callback for event %s, type %s not implemented",
                        callbackRequest.getEventId(),
                        callbackType));
        }
    }

    private AboutToStartOrSubmitCallbackResponse createDocument(
        String authorisation,
        CallbackRequest callbackRequest) {
        CCDCase ccdCase = jsonMapper.fromMap(
            callbackRequest.getCaseDetailsBefore().getData(), CCDCase.class);
        CCDOrderGenerationData ccdOrderGenerationData = jsonMapper.fromMap(
            callbackRequest.getCaseDetails().getData(), CCDOrderGenerationData.class);

        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(templateId)
            .outputType(OutputType.DOC)
            .formPayload(docAssemblyTemplateBodyMapper.from(
                ccdCase, ccdOrderGenerationData, userService.getUserDetails(authorisation)))
            .build();

        DocAssemblyResponse docAssemblyResponse = docAssemblyClient.generateOrder(
            authorisation,
            authTokenGenerator.generate(),
            docAssemblyRequest
        );
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

    private AboutToStartOrSubmitCallbackResponse prepopulateFields() {
        LocalDate deadline = legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines();
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(ImmutableMap.of(
                "directionList", ImmutableList.of(
                    CCDOrderDirectionType.DOCUMENTS.name(),
                    CCDOrderDirectionType.EYEWITNESS.name()
                ),
                "docUploadDeadline", deadline,
                "eyewitnessUploadDeadline", deadline

            ))
            .build();
    }
}
