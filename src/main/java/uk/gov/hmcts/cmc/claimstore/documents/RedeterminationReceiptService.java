package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.ClaimantResponseContentProvider;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForReferToJugdeFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIMANT_RESPONSE_RECEIPT;

@Service
public class RedeterminationReceiptService {

    private final ClaimantResponseContentProvider claimantResponseContentProvider;
    private final DocumentTemplates documentTemplates;
    private final PDFServiceClient pdfServiceClient;

    @Autowired
    public RedeterminationReceiptService(
        ClaimantResponseContentProvider claimantResponseContentProvider,
        DocumentTemplates documentTemplates,
        PDFServiceClient pdfServiceClient
    ) {
        this.claimantResponseContentProvider = claimantResponseContentProvider;
        this.documentTemplates = documentTemplates;
        this.pdfServiceClient = pdfServiceClient;
    }

    public PDF createPdf(Claim claim, MadeBy partyType) {
        requireNonNull(claim);
        return new PDF(
            buildRequestForReferToJugdeFileBaseName(claim.getReferenceNumber(),
                partyType.name().toLowerCase()),
            pdfServiceClient.generateFromHtml(
                documentTemplates.getClaimantResponseReceipt(),
                claimantResponseContentProvider.createContent(claim)),
            CLAIMANT_RESPONSE_RECEIPT
        );
    }

}
