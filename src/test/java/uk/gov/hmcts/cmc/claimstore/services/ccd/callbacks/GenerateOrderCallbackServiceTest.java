package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.LegalOrderGenerationDeadlinesCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.DocAssemblyClient;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyRequest;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.domain.OutputType;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;

@RunWith(MockitoJUnitRunner.class)
public class GenerateOrderCallbackServiceTest {

    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String SERVICE_TOKEN = "Bearer service let me in";
    private static final LocalDate DEADLINE = LocalDate.parse("2018-11-16");
    private static final String DOC_URL = "http://success.test";
    private static final UserDetails JUDGE = new UserDetails(
        "1",
        "email",
        "Judge",
        "McJudge",
        Collections.emptyList());

    @Mock
    private LegalOrderGenerationDeadlinesCalculator legalOrderGenerationDeadlinesCalculator;
    @Mock
    private DocAssemblyClient docAssemblyClient;
    @Mock
    private UserService userService;
    @Mock
    private JsonMapper jsonMapper;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;

    private GenerateOrderCallbackService generateOrderCallbackService;

    @Before
    public void setUp() {
        generateOrderCallbackService = new GenerateOrderCallbackService(
            userService,
            legalOrderGenerationDeadlinesCalculator,
            docAssemblyClient,
            authTokenGenerator,
            jsonMapper,
            docAssemblyTemplateBodyMapper
        );
        ReflectionTestUtils.setField(generateOrderCallbackService, "templateId", "testTemplateId");
        when(legalOrderGenerationDeadlinesCalculator.calculateOrderGenerationDeadlines())
            .thenReturn(DEADLINE);
        when(userService.getUserDetails(BEARER_TOKEN)).thenReturn(JUDGE);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    @Test
    public void shouldPrepopulateFieldsOnAboutToStartEvent() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(GENERATE_ORDER.getValue())
            .build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            generateOrderCallbackService
                .execute(CallbackType.ABOUT_TO_START,
                    callbackRequest,
                    BEARER_TOKEN);

        assertThat(response.getData()).contains(
            entry("directionList", ImmutableList.of("DOCUMENTS", "EYEWITNESS")),
            entry("docUploadDeadline", DEADLINE),
            entry("eyewitnessUploadDeadline", DEADLINE)
        );
    }

    @Test
    public void shouldGenerateDocumentOnMidEvent() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        CCDOrderGenerationData ccdOrderGenerationData = SampleData.getCCDOrderGenerationData();
        when(jsonMapper.fromMap(Collections.emptyMap(), CCDCase.class)).thenReturn(ccdCase);
        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId("testTemplateId")
            .outputType(OutputType.DOC)
            .formPayload(docAssemblyTemplateBodyMapper
                .from(ccdCase, ccdOrderGenerationData, userService.getUserDetails(BEARER_TOKEN)))
            .build();

        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(GENERATE_ORDER.getValue())
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .caseDetailsBefore(CaseDetails.builder().data(Collections.emptyMap()).build())
            .build();

        DocAssemblyResponse docAssemblyResponse = Mockito.mock(DocAssemblyResponse.class);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
        when(docAssemblyClient
            .generateOrder(BEARER_TOKEN, SERVICE_TOKEN, docAssemblyRequest))
            .thenReturn(docAssemblyResponse);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) generateOrderCallbackService
                .execute(CallbackType.MID,
                callbackRequest,
                BEARER_TOKEN);

        CCDDocument document = CCDDocument.builder().documentUrl(DOC_URL).build();
        assertThat(response.getData()).containsExactly(
            entry("draftOrderDoc", document)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfUnimplementedCallbackForValidEvent() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(GENERATE_ORDER.getValue())
            .build();
        generateOrderCallbackService
            .execute(CallbackType.SUBMITTED,
                callbackRequest,
                "Bearer auth");
    }
}
