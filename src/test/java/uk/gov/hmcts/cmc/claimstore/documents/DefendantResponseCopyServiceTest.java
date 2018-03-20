package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantResponseContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseCopyServiceTest {

    @Mock
    private DefendantResponseContentProvider contentProvider;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private Claim claim;

    private DefendantResponseCopyService service;

    @Before
    public void beforeEachTest() {
        service = new DefendantResponseCopyService(contentProvider, documentTemplates, pdfServiceClient);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.createPdf(null);
    }

    @Test
    public void shouldUseCorrectTemplateToCreateTheDocument() {
        service.createPdf(claim);

        verify(documentTemplates).getDefendantResponseReceipt();
    }

}
