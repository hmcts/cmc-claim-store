package uk.gov.hmcts.cmc.claimstore.documents.questionnaire;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.PdfService;
import uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire.DirectionsQuestionnaireContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import java.util.HashMap;
import java.util.Map;

@Component
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

        claim.getClaimantResponse().orElseThrow(IllegalStateException::new);

        ResponseRejection responseRejection = claim.getClaimantResponse()
            .filter(ResponseRejection.class::isInstance)
            .map(ResponseRejection.class::cast)
            .orElseThrow(IllegalStateException::new);

        Map<String, Object> content = new HashMap<>();
        content.put("hearingContent", contentProvider.mapDirectionQuestionnaire
                                        .apply(responseRejection.getDirectionsQuestionnaire().orElseGet(null)) );
        return pdfServiceClient.generateFromHtml(documentTemplates.getClaimantDirectionsQuestionnaire(), content);
    }
}
