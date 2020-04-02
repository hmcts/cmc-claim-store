package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.ClaimantResponseContentProvider;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RedeterminationReceiptServiceTest {

    @Mock
    private ClaimantResponseContentProvider claimantResponseContentProvider;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private  PDFServiceClient pdfServiceClient;

    private RedeterminationReceiptService redeterminationReceiptService;

    @BeforeEach
    void setUp() {
        redeterminationReceiptService = new RedeterminationReceiptService(
            claimantResponseContentProvider,
            documentTemplates,
            pdfServiceClient
        );
    }

    @Test
    void shouldThrowNullPointerWhenGivenNullClaim() {
        assertThrows(NullPointerException.class,
            () -> redeterminationReceiptService.createPdf(null, MadeBy.CLAIMANT));
    }

    @Test
    void shouldUseCorrectTemplateForRedeterminationByClaimant() {
        redeterminationReceiptService.createPdf(SampleClaim.getDefault(), MadeBy.CLAIMANT);
        verify(documentTemplates).getClaimantResponseReceipt();
    }

    @Test
    void shouldUseCorrectTemplateForRedeterminationByDefendant() {
        redeterminationReceiptService.createPdf(SampleClaim.getDefault(), MadeBy.DEFENDANT);
        verify(documentTemplates).getClaimantResponseReceipt();
    }
}
