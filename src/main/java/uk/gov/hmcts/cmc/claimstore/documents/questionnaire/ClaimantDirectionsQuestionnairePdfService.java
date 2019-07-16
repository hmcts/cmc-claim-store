package uk.gov.hmcts.cmc.claimstore.documents.questionnaire;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.PdfService;
import uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire.DirectionsQuestionnaireContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

public class ClaimantDirectionsQuestionnairePdfService implements PdfService {

    private final DocumentTemplates documentTemplates;
    private final PDFServiceClient pdfServiceClient;
    private final DirectionsQuestionnaireContentProvider contentProvider;

    @Autowired
    public ClaimantDirectionsQuestionnairePdfService(DocumentTemplates documentTemplates,
                                                     PDFServiceClient pdfServiceClient,
                                                     DirectionsQuestionnaireContentProvider contentProvider) {
        this.documentTemplates = documentTemplates;
        this.pdfServiceClient = pdfServiceClient;
        this.contentProvider = contentProvider;
    }

    @Override
    public byte[] createPdf(Claim claim) {
        claim.getClaimantResponse(ClaimantResponse::)
            .map(DirectionsQuestionnaireContentProvider::)
        return pdfServiceClient.generateFromHtml(documentTemplates.getClaimantDirectionsQuestionnaire(),
            contentProvider.mapDirectionQuestionnaire.apply(claim.get))
    }
}
