package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.docassembly.DocAssemblyClient;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyRequest;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.domain.OutputType;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocAssemblyServiceTest {
    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String SERVICE_TOKEN = "Bearer service let me in";
    private static final String DOC_URL = "http://success.test";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";
    private static final String JURISDICTION_ID = "CMC";
    private static final String TEMPLATE_ID = "templateId";

    private static final UserDetails JUDGE = new UserDetails(
        "1",
        "email",
        "Judge",
        "McJudge",
        Collections.emptyList());
    private static final String JUDGE_TEMPLATE_ID = "JudgeTemplateId";

    @Mock
    private DocAssemblyClient docAssemblyClient;
    @Mock
    private UserService userService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private DocAssemblyResponse docAssemblyResponse;

    private DocAssemblyService docAssemblyService;

    private CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());

    @Before
    public void setup() {
        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    @Test
    public void shouldRenderSecureTemplate() {
        docAssemblyService = new DocAssemblyService(authTokenGenerator, docAssemblyClient, true);
        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);
        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(TEMPLATE_ID)
            .outputType(OutputType.PDF)
            .formPayload(docAssemblyTemplateBody)
            .caseTypeId(CASE_TYPE_ID)
            .jurisdictionId(JURISDICTION_ID)
            .secureDocStoreEnabled(true)
            .build();

        when(docAssemblyClient
            .generateOrder(eq(BEARER_TOKEN), eq(SERVICE_TOKEN), eq(docAssemblyRequest)))
            .thenReturn(docAssemblyResponse);

        DocAssemblyResponse response = docAssemblyService.renderTemplate(ccdCase, BEARER_TOKEN, TEMPLATE_ID,
            CASE_TYPE_ID, JURISDICTION_ID,
            docAssemblyTemplateBody);

        assertThat(response.getRenditionOutputLocation()).isEqualTo(DOC_URL);

        verify(docAssemblyClient).generateOrder(eq(BEARER_TOKEN), eq(SERVICE_TOKEN), any(DocAssemblyRequest.class));
    }

    @Test
    public void shouldGenerateSecureDocument() {
        docAssemblyService = new DocAssemblyService(authTokenGenerator, docAssemblyClient, true);
        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);
        DocAssemblyTemplateBody formPayload = mock(DocAssemblyTemplateBody.class);

        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(TEMPLATE_ID)
            .outputType(OutputType.PDF)
            .formPayload(formPayload)
            .caseTypeId(CASE_TYPE_ID)
            .jurisdictionId(JURISDICTION_ID)
            .secureDocStoreEnabled(true)
            .build();

        when(docAssemblyClient
            .generateOrder(eq(BEARER_TOKEN), eq(SERVICE_TOKEN), eq(docAssemblyRequest)))
            .thenReturn(docAssemblyResponse);

        CCDDocument document = docAssemblyService.generateDocument(ccdCase, BEARER_TOKEN, formPayload,
            TEMPLATE_ID, CASE_TYPE_ID, JURISDICTION_ID);

        assertEquals(DOC_URL, document.getDocumentUrl());
    }

    @Test
    public void shouldRenderTemplate() {
        docAssemblyService = new DocAssemblyService(authTokenGenerator, docAssemblyClient, false);
        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);
        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(TEMPLATE_ID)
            .outputType(OutputType.PDF)
            .formPayload(docAssemblyTemplateBody)
            .build();

        when(docAssemblyClient
            .generateOrder(eq(BEARER_TOKEN), eq(SERVICE_TOKEN), eq(docAssemblyRequest)))
            .thenReturn(docAssemblyResponse);

        DocAssemblyResponse response = docAssemblyService.renderTemplate(ccdCase, BEARER_TOKEN, TEMPLATE_ID,
            CASE_TYPE_ID, JURISDICTION_ID,
            docAssemblyTemplateBody);

        assertThat(response.getRenditionOutputLocation()).isEqualTo(DOC_URL);

        verify(docAssemblyClient).generateOrder(eq(BEARER_TOKEN), eq(SERVICE_TOKEN), any(DocAssemblyRequest.class));
    }

    @Test
    public void shouldGenerateDocument() {
        docAssemblyService = new DocAssemblyService(authTokenGenerator, docAssemblyClient, false);
        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);
        DocAssemblyTemplateBody formPayload = mock(DocAssemblyTemplateBody.class);

        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(TEMPLATE_ID)
            .outputType(OutputType.PDF)
            .formPayload(formPayload)
            .build();

        when(docAssemblyClient
            .generateOrder(eq(BEARER_TOKEN), eq(SERVICE_TOKEN), eq(docAssemblyRequest)))
            .thenReturn(docAssemblyResponse);

        CCDDocument document = docAssemblyService.generateDocument(ccdCase, BEARER_TOKEN, formPayload,
            TEMPLATE_ID, CASE_TYPE_ID, JURISDICTION_ID);

        assertEquals(DOC_URL, document.getDocumentUrl());
    }
}
