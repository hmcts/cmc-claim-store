package uk.gov.hmcts.cmc.claimstore.documents.questionnaire;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire.ClaimantDirectionsQuestionnaireContentProvider;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ClaimantDirectionsQuestionnairePdfServiceTest {

    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private ClaimantDirectionsQuestionnaireContentProvider claimantDirectionsQuestionnaireContentProvider;

    private ClaimantDirectionsQuestionnairePdfService claimantDirectionsQuestionnairePdfService;

    @BeforeEach
    public void setUp() {
        claimantDirectionsQuestionnairePdfService = new ClaimantDirectionsQuestionnairePdfService(
            new DocumentTemplates(),
            pdfServiceClient,
            claimantDirectionsQuestionnaireContentProvider
        );
    }

    @Test
    public void createPdfThrowsExceptionWhenResponseIsNull() {
        assertThrows(IllegalStateException.class, () -> {
            claimantDirectionsQuestionnairePdfService.createPdf(SampleClaim.getDefault());
        });
    }

    @Test
    public void createPdfThrowsExceptionWhenResponseIsAcceptation() {
        assertThrows(IllegalArgumentException.class, () -> {
            claimantDirectionsQuestionnairePdfService.createPdf(SampleClaim.getWithClaimantResponse());
        });
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
