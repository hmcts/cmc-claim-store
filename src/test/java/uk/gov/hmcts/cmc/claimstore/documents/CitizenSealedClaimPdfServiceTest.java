package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.EmailContentTemplates;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.SealedClaimContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CitizenSealedClaimPdfServiceTest {

    @Mock
    private SealedClaimContentProvider contentProvider;
    @Mock
    private EmailContentTemplates emailTemplates;
    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private Claim claim;

    private CitizenSealedClaimPdfService service;

    @Before
    public void beforeEachTest() {
        service = new CitizenSealedClaimPdfService(emailTemplates, pdfServiceClient, contentProvider);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.createPdf(null, null);
    }

    @Test
    public void shouldUseCorrectTemplateToCreateTheDocument() {
        service.createPdf(claim, "submitter@email.com");
        verify(emailTemplates).getSealedClaim();
    }

}
