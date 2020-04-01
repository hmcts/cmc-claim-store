package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.ClaimantResponseContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForInterlocutoryJudgmentFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIMANT_RESPONSE_RECEIPT;

@Service
public class InterlocutoryReceiptService {

    private final ClaimantResponseContentProvider contentProvider;
    private final DocumentTemplates documentTemplates;
    private final PDFServiceClient pdfServiceClient;

    @Autowired
    public InterlocutoryReceiptService(
        ClaimantResponseContentProvider contentProvider,
        DocumentTemplates documentTemplates,
        PDFServiceClient pdfServiceClient
    ) {
        this.contentProvider = contentProvider;
        this.documentTemplates = documentTemplates;
        this.pdfServiceClient = pdfServiceClient;
    }

    public PDF createPdf(Claim claim) {
        requireNonNull(claim);
        return new PDF(
            buildRequestForInterlocutoryJudgmentFileBaseName(claim.getReferenceNumber()),
            pdfServiceClient.generateFromHtml(
                documentTemplates.getClaimantResponseReceipt(),
                contentProvider.createContent(claim)),
            CLAIMANT_RESPONSE_RECEIPT
        );
    }

}
