package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.LegalSealedClaimContentProvider;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;

@Service
public class LegalSealedClaimService {

    private final StaffEmailTemplates emailTemplates;
    private final PDFServiceClient pdfServiceClient;
    private final LegalSealedClaimContentProvider legalSealedClaimContentProvider;
    private final boolean watermarkPdfEnabled;

    @Autowired
    public LegalSealedClaimService(
        final StaffEmailTemplates emailTemplates,
        final PDFServiceClient pdfServiceClient,
        final LegalSealedClaimContentProvider legalSealedClaimContentProvider,
        @Value("${claim-store.watermark-pdf.enabled}") final boolean watermarkPdfEnabled
    ) {
        this.emailTemplates = emailTemplates;
        this.pdfServiceClient = pdfServiceClient;
        this.legalSealedClaimContentProvider = legalSealedClaimContentProvider;
        this.watermarkPdfEnabled = watermarkPdfEnabled;
    }

    public byte[] createPdf(Claim claim) {
        requireNonNull(claim);

        final byte[] legalSealedClaim = watermarkPdfEnabled
            ? emailTemplates.getWaterMarkedLegalSealedClaim()
            :emailTemplates.getLegalSealedClaim();

        return pdfServiceClient.generateFromHtml(
            legalSealedClaim,
            legalSealedClaimContentProvider.createContent(claim)
        );
    }
}
