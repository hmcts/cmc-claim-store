package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.EmailContentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.LegalSealedClaimContentProvider;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LegalSealedClaimPdfServiceTest {

    @Mock
    private LegalSealedClaimContentProvider contentProvider;
    @Mock
    private EmailContentTemplates emailTemplates;
    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private Claim claim;

    private LegalSealedClaimPdfService service;

    @Before
    public void beforeEachTest() {
        service = new LegalSealedClaimPdfService(emailTemplates, pdfServiceClient, contentProvider);
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

}
