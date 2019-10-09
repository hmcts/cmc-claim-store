package uk.gov.hmcts.cmc.claimstore.documents.questionnaire;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire.ClaimantDirectionsQuestionnaireContentProvider;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantDirectionsQuestionnairePdfServiceTest {

    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private ClaimantDirectionsQuestionnaireContentProvider claimantDirectionsQuestionnaireContentProvider;

    private ClaimantDirectionsQuestionnairePdfService claimantDirectionsQuestionnairePdfService;

    @Before
    public void setUp() {
        claimantDirectionsQuestionnairePdfService = new ClaimantDirectionsQuestionnairePdfService(
            new DocumentTemplates(),
            pdfServiceClient,
            claimantDirectionsQuestionnaireContentProvider
        );
    }

    @Test(expected = IllegalStateException.class)
    public void createPdfThrowsExceptionWhenResponseIsNull() {
        claimantDirectionsQuestionnairePdfService.createPdf(SampleClaim.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPdfThrowsExceptionWhenResponseIsAcceptation() {
        claimantDirectionsQuestionnairePdfService.createPdf(SampleClaim.getWithClaimantResponse());
    }

    @Test
    public void createPdfCreatesHearingContent() {
        Mockito.when(pdfServiceClient.generateFromHtml(Mockito.any(), Mockito.anyMap()))
            .thenReturn("DoNothing".getBytes());
        claimantDirectionsQuestionnairePdfService.createPdf(
            SampleClaim.getWithClaimantResponse(
                SampleClaimantResponse.ClaimantResponseRejection.builder()
                    .buildRejectionWithDirectionsQuestionnaire()
            )
        );

        verify(pdfServiceClient).generateFromHtml(Mockito.any(), Mockito.anyMap());
    }
}
