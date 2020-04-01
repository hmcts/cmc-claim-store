package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.ClaimantResponseContentProvider;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class InterlocutoryReceiptServiceTest {

    @Mock
    private ClaimantResponseContentProvider contentProvider;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private PDFServiceClient pdfServiceClient;

    private InterlocutoryReceiptService interlocutoryReceiptService;

    @BeforeEach
    void setUp() {
        interlocutoryReceiptService = new InterlocutoryReceiptService(
            contentProvider,
            documentTemplates,
            pdfServiceClient
        );
    }

    @Test
    void shouldThrowNullPointerWhenGivenNullClaim() {
        assertThrows(NullPointerException.class,
            () -> interlocutoryReceiptService.createPdf(null));
    }

    @Test
    void shouldUseCorrectTemplateForCCJRequest() {
        interlocutoryReceiptService.createPdf(SampleClaim.getDefault());
        verify(documentTemplates).getClaimantResponseReceipt();
    }
}
