package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.helper.HTMLTemplateProcessor;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import java.time.LocalDateTime;
import java.util.Map;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.helper.FileUtils.readFile;
import static uk.gov.hmcts.cmc.claimstore.helper.FileUtils.writeFile;
import static wiremock.org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

@RunWith(SpringRunner.class)
public class DefendantResponseReceiptServiceIT extends BaseMockSpringTest {

    private static final String TIMESTAMP_ATTRIBUTE_VALUE = "timestamp";
    private static final String DUMMY_TIMESTAMP = "TIMESTAMP";

    @Autowired
    private DefendantResponseReceiptService defendantResponseReceiptService;

    @Autowired
    protected HTMLTemplateProcessor htmlTemplateProcessor;

    @MockBean
    protected EmailService emailService;

    @Test
    public void shouldGenerateDefenceResponseForClaimWithFullDefenceAlreadyPaid() throws Exception {

        provideLocalPdfService();

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

        String actualHtml = replaceTimestampsWithFixedValues(new String(actualHtmlBytes));

        // Useful for debugging test ie diff comparison of actual with expected HTML file
        writeFile("build/tmp/actual.html", actualHtml);

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

    public String replaceTimestampsWithFixedValues(String html) {

        final String timestampElement = String.format("data-type=\"%s\"", TIMESTAMP_ATTRIBUTE_VALUE);
        final String regex = format("%s\\>.*\\<", timestampElement);
        final String replacement = format("%s\\>%s\\<", timestampElement, DUMMY_TIMESTAMP);

        return html.replaceAll(regex, replacement);
    }
}
