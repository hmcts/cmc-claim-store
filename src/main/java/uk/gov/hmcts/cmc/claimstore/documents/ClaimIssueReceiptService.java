package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimIssueReceiptFileBaseName;

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

    public byte[] createPdf(Claim claim) {
        requireNonNull(claim);

        return pdfServiceClient.generateFromHtml(
            documentTemplates.getClaimIssueReceipt(),
            claimContentProvider.createContent(claim)
        );
    }

    @Override
    public String filename(Claim claim) {
        return buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber());
    }

}
