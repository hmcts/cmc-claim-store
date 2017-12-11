package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;

@Service
public class CitizenSealedClaimPdfService {

    private final DocumentTemplates documentTemplates;
    private final PDFServiceClient pdfServiceClient;
    private final ClaimContentProvider claimContentProvider;

    @Autowired
    public CitizenSealedClaimPdfService(
        final DocumentTemplates documentTemplates,
        final PDFServiceClient pdfServiceClient,
        final ClaimContentProvider claimContentProvider
    ) {
        this.documentTemplates = documentTemplates;
        this.pdfServiceClient = pdfServiceClient;
        this.claimContentProvider = claimContentProvider;
    }

    public byte[] createPdf(final Claim claim) {
        requireNonNull(claim);

        return pdfServiceClient.generateFromHtml(
            documentTemplates.getSealedClaim(),
            claimContentProvider.createContent(claim)
        );
    }
}
