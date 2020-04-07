package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantResponseContentProvider;
import uk.gov.hmcts.cmc.claimstore.helper.DocumentComparisonHelper;
import uk.gov.hmcts.cmc.claimstore.helper.HTMLTemplateProcessor;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.helper.FileUtils.readFile;
import static uk.gov.hmcts.cmc.claimstore.helper.FileUtils.writeFile;
import static wiremock.org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

@RunWith(SpringRunner.class)
public class DefendantResponseReceiptServiceIntegrationTest extends BaseMockSpringTest {

    @Autowired
    private DefendantResponseReceiptService defendantResponseReceiptService;

    @Autowired
    protected DefendantResponseContentProvider contentProvider;

    @Autowired
    protected DocumentTemplates documentTemplates;

    @Autowired
    protected DocumentComparisonHelper documentComparisonHelper;

    @Autowired
    protected HTMLTemplateProcessor htmlTemplateProcessor;

    @MockBean
    protected EmailService emailService;

    @Before
    public void beforeEachTest() {

        provideLocalPdfService();
    }

    @Test
    public void shouldGenerateDefenceResponseForClaimWithFullDefenceAlreadyPaid() throws Exception {

        Claim claim = SampleClaim.builder()
            .withResponse(
                SampleResponse.FullDefence
                    .builder()
                    .withDefenceType(DefenceType.ALREADY_PAID)
                    .withMediation(null)
                    .build()
            )
            .withRespondedAt(LocalDateTime.now())
            .build();

        byte[] actualHtmlBytes = defendantResponseReceiptService.createHtml(claim);

        String actualHtml = documentComparisonHelper.replaceTimestamp("defenceSubmittedOn",
            new String(actualHtmlBytes));

        writeFile("build/tmp/actual.html", actualHtml); // Useful file for debugging test

        String expectedHtml = readFile("src/integrationTest/resources/documents/expectedDefendantResponseReceipt.html");

        assertXMLEqual(actualHtml, expectedHtml);
    }

    private PDFServiceClient provideLocalPdfService() {

        when(pdfServiceClient.generateFromHtml(any(), any()))
            .thenAnswer((Answer<byte[]>) invocation -> {
                Object[] args = invocation.getArguments();
                byte[] template = (byte[]) args[0];
                Map<String, Object> placeholders = (Map<String, Object>) args[1];

                String output = htmlTemplateProcessor.process(new String(template), placeholders);
                return output.getBytes();
            });

        return pdfServiceClient;
    }
}
