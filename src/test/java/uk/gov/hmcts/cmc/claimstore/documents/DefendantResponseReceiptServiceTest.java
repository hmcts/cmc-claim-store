package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantResponseContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.pdf.PDFServiceClient;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class DefendantResponseReceiptServiceTest {

    @Mock
    private DefendantResponseContentProvider contentProvider;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private PDFServiceClient pdfServiceClient;

    private DefendantResponseReceiptService defendantResponseReceiptService;

    @BeforeEach
    public void setUp() {
        defendantResponseReceiptService = new DefendantResponseReceiptService(
            contentProvider,
            documentTemplates,
            pdfServiceClient
        );
    }

    @Test
    public void shouldThrowErrorWhenDefendantResponseDoesNotExist() {
        Claim claim = SampleClaim.builder().build();
        try {
            defendantResponseReceiptService.createPdf(claim);
            Assertions.fail("Expected a NotFoundException to be thrown");
        } catch (NotFoundException expected) {
            assertThat(expected).hasMessage("Defendant response does not exist for this claim");
        }
    }

}
