package uk.gov.hmcts.cmc.claimstore.documents.questionnaire;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.PdfService;
import uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire.ClaimantDirectionsQuestionnaireContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

@Component
public class ClaimantDirectionsQuestionnairePdfService implements PdfService {

    private final DocumentTemplates documentTemplates;
    private final PDFServiceClient pdfServiceClient;
    private final ClaimantDirectionsQuestionnaireContentProvider contentProvider;

    @Autowired
    public ClaimantDirectionsQuestionnairePdfService(DocumentTemplates documentTemplates,
                                                     PDFServiceClient pdfServiceClient,
                                                     ClaimantDirectionsQuestionnaireContentProvider contentProvider) {
        this.documentTemplates = documentTemplates;
        this.pdfServiceClient = pdfServiceClient;
        this.contentProvider = contentProvider;
    }

    @Override
    public byte[] createPdf(Claim claim) {

        claim.getClaimantResponse().orElseThrow(IllegalStateException::new);

        claim.getClaimantResponse()
            .filter(ResponseRejection.class::isInstance)
            .map(ResponseRejection.class::cast)
            .orElseThrow(IllegalArgumentException::new);

        return pdfServiceClient.generateFromHtml(documentTemplates.getClaimantDirectionsQuestionnaire(),
            contentProvider.createContent(claim));
    }
}
