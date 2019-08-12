package uk.gov.hmcts.cmc.claimstore.documents.questionnaire;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.PdfService;
import uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire.ClaimantDirectionsQuestionnaireContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimantHearingFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIMANT_DIRECTIONS_QUESTIONNAIRE;

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
    public PDF createPdf(Claim claim) {

        requireNonNull(claim.getClaimantResponse());
        if (!claim.getClaimantResponse().isPresent()) {
            throw new IllegalStateException("Claimant Response should be present");
        }

        if (!claim.getClaimantResponse()
            .filter(ResponseRejection.class::isInstance)
            .map(ResponseRejection.class::cast).isPresent()) {
            throw new IllegalArgumentException("Response should be of type Rejection");
        }

        return new PDF(buildClaimantHearingFileBaseName(claim.getReferenceNumber()),
            pdfServiceClient.generateFromHtml(documentTemplates.getClaimantDirectionsQuestionnaire(),
                contentProvider.createContent(claim)),
            CLAIMANT_DIRECTIONS_QUESTIONNAIRE);
    }
}
