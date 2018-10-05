package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.ContentProvider;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CountyCourtJudgmentPdfServiceTest {

    @Mock
    private DocumentTemplates documentTemplates;

    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private ContentProvider contentProvider;

    private CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;

    @Before
    public void beforeTest() {
        countyCourtJudgmentPdfService = new CountyCourtJudgmentPdfService(
            documentTemplates,
            pdfServiceClient,
            contentProvider
        );
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        countyCourtJudgmentPdfService.createPdf(null);
    }

    @Test
    public void shouldUseCorrectTemplateForCCJRequest() {
        countyCourtJudgmentPdfService.createPdf(SampleClaim.getDefault());
        verify(documentTemplates).getCountyCourtJudgmentByRequest();
    }
}
