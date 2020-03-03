package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment.ContentProvider;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CcjByAdmissionOrDeterminationPdfServiceTest {

    @Mock
    private DocumentTemplates documentTemplates;

    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private ContentProvider contentProvider;

    private CcjByAdmissionOrDeterminationPdfService ccjByAdmissionOrDeterminationPdfService;

    @BeforeEach
    public void beforeTest() {
        ccjByAdmissionOrDeterminationPdfService = new CcjByAdmissionOrDeterminationPdfService(
            documentTemplates,
            pdfServiceClient,
            contentProvider
        );
    }

    @Test
    void shouldThrowNullPointerWhenGivenNullClaim() {
        assertThrows(NullPointerException.class,
            () -> ccjByAdmissionOrDeterminationPdfService.createPdf(null));
    }

    @Test
    void shouldUseCorrectTemplateForCCJRequest() {
        ccjByAdmissionOrDeterminationPdfService.createPdf(SampleClaim.getDefault());
        verify(documentTemplates).getCountyCourtJudgmentByRequest();
    }
}
