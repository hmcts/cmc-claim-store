package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.ReviewOrderContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildReviewOrderFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.REVIEW_ORDER;

@Service
public class ReviewOrderService implements PdfService {

    private final DocumentTemplates documentTemplates;
    private final PDFServiceClient pdfServiceClient;
    private final ReviewOrderContentProvider contentProvider;

    @Autowired
    public ReviewOrderService(
        DocumentTemplates documentTemplates,
        PDFServiceClient pdfServiceClient,
        ReviewOrderContentProvider contentProvider
    ) {
        this.documentTemplates = documentTemplates;
        this.pdfServiceClient = pdfServiceClient;
        this.contentProvider = contentProvider;
    }

    public PDF createPdf(Claim claim) {
        requireNonNull(claim);

        return new PDF(
            buildReviewOrderFileBaseName(claim.getReferenceNumber()),
            pdfServiceClient.generateFromHtml(
                documentTemplates.getReviewOrder(),
                contentProvider.createContent(claim)),
            REVIEW_ORDER
        );
    }
}
