package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.EmailContentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.LegalSealedClaimContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;

@Service
public class LegalSealedClaimPdfService {

    private final EmailContentTemplates emailTemplates;
    private final PDFServiceClient pdfServiceClient;
    private final LegalSealedClaimContentProvider legalSealedClaimContentProvider;

    @Autowired
    public LegalSealedClaimPdfService(
        final EmailContentTemplates emailTemplates,
        final PDFServiceClient pdfServiceClient,
        final LegalSealedClaimContentProvider legalSealedClaimContentProvider
    ) {
        this.emailTemplates = emailTemplates;
        this.pdfServiceClient = pdfServiceClient;
        this.legalSealedClaimContentProvider = legalSealedClaimContentProvider;
    }

    public byte[] createPdf(Claim claim) {
        requireNonNull(claim);
        return pdfServiceClient.generateFromHtml(
            emailTemplates.getLegalSealedClaim(),
            legalSealedClaimContentProvider.createContent(claim)
        );
    }
}
