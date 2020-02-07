package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.docassembly.DocAssemblyClient;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyRequest;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.domain.OutputType;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocAssemblyServiceTest {
    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String SERVICE_TOKEN = "Bearer service let me in";
    private static final String DOC_URL = "http://success.test";
    public static final String LEGAL_ADVISOR_TEMPLATE_ID = "legalAdvisorTemplateId";
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
    private DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;

    private DocAssemblyService docAssemblyService;

    @Before
    public void setup() {
        docAssemblyService = new DocAssemblyService(authTokenGenerator,
            docAssemblyTemplateBodyMapper,
            docAssemblyClient,
            userService,
            LEGAL_ADVISOR_TEMPLATE_ID,
            JUDGE_TEMPLATE_ID);

        when(userService.getUserDetails(eq(BEARER_TOKEN))).thenReturn(JUDGE);
    }

    @Test
    public void shouldCreateOrderOnDocAssembly() {
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase = SampleData.addCCDOrderGenerationData(ccdCase);

        when(docAssemblyTemplateBodyMapper.from(eq(ccdCase), eq(JUDGE)))
            .thenReturn(DocAssemblyTemplateBody.builder().build());

        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        DocAssemblyRequest docAssemblyRequest = DocAssemblyRequest.builder()
            .templateId(LEGAL_ADVISOR_TEMPLATE_ID)
            .outputType(OutputType.PDF)
            .formPayload(docAssemblyTemplateBodyMapper.from(ccdCase, JUDGE))
            .build();

        DocAssemblyResponse docAssemblyResponse = Mockito.mock(DocAssemblyResponse.class);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
        when(docAssemblyClient
            .generateOrder(eq(BEARER_TOKEN), eq(SERVICE_TOKEN), eq(docAssemblyRequest)))
            .thenReturn(docAssemblyResponse);

        DocAssemblyResponse response = docAssemblyService.createOrder(ccdCase, BEARER_TOKEN);

        assertThat(response.getRenditionOutputLocation()).isEqualTo(DOC_URL);

        verify(docAssemblyClient).generateOrder(eq(BEARER_TOKEN), eq(SERVICE_TOKEN), any(DocAssemblyRequest.class));
    }
}
