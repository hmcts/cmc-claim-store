package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.LegalSealedClaimContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;

@Service
public class LegalSealedClaimPdfService {

    private final DocumentTemplates documentTemplates;
    private final PDFServiceClient pdfServiceClient;
    private final LegalSealedClaimContentProvider legalSealedClaimContentProvider;

    @Autowired
    public LegalSealedClaimPdfService(
        DocumentTemplates documentTemplates,
        PDFServiceClient pdfServiceClient,
        LegalSealedClaimContentProvider legalSealedClaimContentProvider
    ) {
        this.documentTemplates = documentTemplates;
        this.pdfServiceClient = pdfServiceClient;
        this.legalSealedClaimContentProvider = legalSealedClaimContentProvider;
    }

    public byte[] createPdf(Claim claim) {
        requireNonNull(claim);
        return pdfServiceClient.generateFromHtml(
            documentTemplates.getLegalSealedClaim(),
            legalSealedClaimContentProvider.createContent(claim)
        );
    }
}
