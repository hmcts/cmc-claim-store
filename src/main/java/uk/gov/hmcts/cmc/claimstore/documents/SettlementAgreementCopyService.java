package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement.SettlementAgreementPDFContentProvider;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSettlementReachedFileBaseName;

@Service
public class SettlementAgreementCopyService implements PdfService {

    private final SettlementAgreementPDFContentProvider contentProvider;
    private final DocumentTemplates documentTemplates;
    private final PDFServiceClient pdfServiceClient;

    @Autowired
    public SettlementAgreementCopyService(
        SettlementAgreementPDFContentProvider contentProvider,
        DocumentTemplates documentTemplates,
        PDFServiceClient pdfServiceClient
    ) {
        this.contentProvider = contentProvider;
        this.documentTemplates = documentTemplates;
        this.pdfServiceClient = pdfServiceClient;
    }

    public byte[] createPdf(Claim claim) {
        requireNonNull(claim);
        if (!claim.getSettlement().isPresent() && null == claim.getSettlementReachedAt()) {
            throw new NotFoundException("Settlement Agreement does not exist for this claim");
        }
        byte[] settlementAgreement = documentTemplates.getSettlementAgreement();
        return pdfServiceClient.generateFromHtml(
            settlementAgreement,
            contentProvider.createContent(claim)
        );
    }

    @Override
    public String filename(Claim claim) {
        return buildSettlementReachedFileBaseName(claim.getReferenceNumber());
    }

}
