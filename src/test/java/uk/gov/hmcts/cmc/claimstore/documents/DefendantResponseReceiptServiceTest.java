package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantResponseContentProvider;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseReceiptServiceTest {

    @Mock
    private DefendantResponseContentProvider contentProvider;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private PDFServiceClient pdfServiceClient;

    private DefendantResponseReceiptService defendantResponseReceiptService;

    @Before
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
            Assert.fail("Expected a NotFoundException to be thrown");
        } catch (NotFoundException expected) {
            assertThat(expected).hasMessage("Defendant response does not exist for this claim");
        }
    }

}
