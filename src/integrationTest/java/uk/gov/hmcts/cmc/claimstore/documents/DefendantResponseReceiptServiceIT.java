package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantResponseContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDateTime;

import static uk.gov.hmcts.cmc.claimstore.helper.DocumentComparisonHelper.provideLocalPdfService;
import static uk.gov.hmcts.cmc.claimstore.helper.DocumentComparisonHelper.replaceTimestamp;
import static uk.gov.hmcts.cmc.claimstore.helper.FileUtils.readFile;
import static uk.gov.hmcts.cmc.claimstore.helper.FileUtils.writeFile;
import static wiremock.org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

public class DefendantResponseReceiptServiceIT extends BaseMockSpringTest {

    private DefendantResponseReceiptService defendantResponseReceiptService;

    @Autowired
    private DefendantResponseContentProvider contentProvider;

    @Autowired
    private DocumentTemplates documentTemplates;

    @Before
    public void beforeEachTest() {

        defendantResponseReceiptService = new DefendantResponseReceiptService(
            contentProvider, documentTemplates, provideLocalPdfService());
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

        String actualHtml = replaceTimestamp("defenceSubmittedOn", new String(actualHtmlBytes));

        writeFile("build/tmp/actual.html", actualHtml); // Useful file for debugging test

        String expectedHtml = readFile("src/integrationTest/resources/documents/expectedDefendantResponseReceipt.html");

        assertXMLEqual(actualHtml, expectedHtml);
    }
}
