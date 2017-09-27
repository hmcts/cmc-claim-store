package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.LegalSealedClaimContentProvider;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LegalSealedClaimServiceTest {

    @Mock
    private LegalSealedClaimContentProvider contentProvider;
    @Mock
    private StaffEmailTemplates emailTemplates;
    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private Claim claim;

    private LegalSealedClaimService service;

    @Before
    public void beforeEachTest() {
        service = new LegalSealedClaimService(emailTemplates, pdfServiceClient, contentProvider, false);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.createPdf(null);
    }

    @Test
    public void shouldUseCorrectTemplateToCreateTheDocument() {
        service.createPdf(claim);
        verify(emailTemplates).getLegalSealedClaim();
    }

    @Test
    public void shouldUseCorrectWaterMarkedTemplateToCreateTheDocument() {
        service = new LegalSealedClaimService(emailTemplates, pdfServiceClient, contentProvider, true);

        service.createPdf(claim);
        verify(emailTemplates).getWaterMarkedLegalSealedClaim();
    }
}
