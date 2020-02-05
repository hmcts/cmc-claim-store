package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantResponseContentProvider;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseReceiptServiceTest {

    @Rule
    public final ExpectedException exceptionRule = ExpectedException.none();
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
        exceptionRule.expect(NotFoundException.class);
        exceptionRule.expectMessage("Defendant response does not exist for this claim");
        defendantResponseReceiptService.createPdf(claim);
    }

}
