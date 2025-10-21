package uk.gov.hmcts.cmc.claimstore.documents;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.ReviewOrderContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.REVIEW_ORDER;

@RunWith(MockitoJUnitRunner.class)
public class ReviewOrderServiceTest {

    @Mock
    private DocumentTemplates documentTemplates;

    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private ReviewOrderContentProvider contentProvider;

    private ReviewOrderService reviewOrderService;

    @Before
    public void setUp() {
        reviewOrderService = new ReviewOrderService(
            documentTemplates,
            pdfServiceClient,
            contentProvider
        );
    }

    @Test
    public void shouldUseCorrectTemplate() {
        Claim claim = SampleClaim.getDefault();
        when(contentProvider.createContent(claim)).thenReturn(Collections.emptyMap());
        when(documentTemplates.getReviewOrder()).thenReturn("template".getBytes());

        PDF pdf = reviewOrderService.createPdf(claim);
        verify(documentTemplates).getReviewOrder();
        verify(pdfServiceClient).generateFromHtml(
            "template".getBytes(),
            Collections.emptyMap()
        );

        Assertions.assertThat(pdf.getClaimDocumentType()).isEqualTo(REVIEW_ORDER);
        Assertions.assertThat(pdf.getFilename()).isEqualTo(claim.getReferenceNumber() + "-review-order.pdf");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfClaimIsNull() {
        reviewOrderService.createPdf(null);
    }

}
