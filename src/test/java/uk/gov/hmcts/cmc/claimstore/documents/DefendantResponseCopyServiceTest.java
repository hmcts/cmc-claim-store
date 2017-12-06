package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantResponseCopyContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseCopyServiceTest {

    @Mock
    private DefendantResponseCopyContentProvider contentProvider;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private Claim claim;

    private DefendantResponseCopyService service;

    @Before
    public void beforeEachTest() {
        service = new DefendantResponseCopyService(contentProvider, pdfServiceClient);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.createPdf(null, documentTemplates.getDefendantResponseCopy());
    }

    @Test
    public void shouldUseCorrectTemplateToCreateTheDocument() {
        service.createPdf(claim, documentTemplates.getDefendantResponseCopy());

        verify(documentTemplates).getDefendantResponseCopy();
    }

}
