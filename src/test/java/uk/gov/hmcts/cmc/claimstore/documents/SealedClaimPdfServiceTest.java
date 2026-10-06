package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.LegalSealedClaimContentProvider;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SealedClaimPdfServiceTest {

    @Mock
    private LegalSealedClaimContentProvider contentProvider;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private PDFServiceClient pdfServiceClient;
    @Mock
    private CitizenServiceDocumentsService documentsService;

    private SealedClaimPdfService service;

    @BeforeEach
    public void beforeEachTest() {
        service = new SealedClaimPdfService(documentTemplates, pdfServiceClient, contentProvider, documentsService);
    }

    @Test
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        assertThrows(NullPointerException.class, () -> {
            service.createPdf(null);
        });
    }

    @Test
    public void shouldUseCorrectTemplateToCreateTheDocument() {
        service.createPdf(SampleClaim.getDefaultForLegal());
        verify(documentTemplates).getLegalSealedClaim();
    }

}
