package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimIssueReceiptFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;

@Service
public class ClaimIssueReceiptService implements PdfService {

    private final DocumentTemplates documentTemplates;
    private final PDFServiceClient pdfServiceClient;
    private final ClaimContentProvider claimContentProvider;

    @Autowired
    public ClaimIssueReceiptService(
        DocumentTemplates documentTemplates,
        PDFServiceClient pdfServiceClient,
        ClaimContentProvider claimContentProvider
    ) {
        this.documentTemplates = documentTemplates;
        this.pdfServiceClient = pdfServiceClient;
        this.claimContentProvider = claimContentProvider;
    }

    public PDF createPdf(Claim claim) {
        requireNonNull(claim);

        return new PDF(
            buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()),
            pdfServiceClient.generateFromHtml(
                documentTemplates.getClaimIssueReceipt(),
                claimContentProvider.createContent(claim)),
            CLAIM_ISSUE_RECEIPT
        );
    }
}
