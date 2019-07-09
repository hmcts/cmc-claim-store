package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.DefendantResponseContentProvider;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;

@Service
public class DefendantResponseReceiptService implements PdfService {

    private final DefendantResponseContentProvider contentProvider;
    private final DocumentTemplates documentTemplates;
    private final PDFServiceClient pdfServiceClient;

    @Autowired
    public DefendantResponseReceiptService(
        DefendantResponseContentProvider contentProvider,
        DocumentTemplates documentTemplates,
        PDFServiceClient pdfServiceClient
    ) {
        this.contentProvider = contentProvider;
        this.documentTemplates = documentTemplates;
        this.pdfServiceClient = pdfServiceClient;
    }

    public byte[] createPdf(Claim claim) {
        requireNonNull(claim);
        if (!claim.getResponse().isPresent() && null == claim.getRespondedAt()) {
            throw new NotFoundException("Defendant response does not exist for this claim");
        }
        return pdfServiceClient.generateFromHtml(
            documentTemplates.getDefendantResponseReceipt(),
            contentProvider.createContent(claim));
    }

    @Override
    public String filename(Claim claim) {
        return buildResponseFileBaseName(claim.getReferenceNumber());
    }
}
