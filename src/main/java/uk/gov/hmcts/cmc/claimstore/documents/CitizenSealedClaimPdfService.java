package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.EmailContentTemplates;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.SealedClaimContentProvider;
import uk.gov.hmcts.reform.cmc.pdf.service.client.PDFServiceClient;

import static java.util.Objects.requireNonNull;

@Service
public class CitizenSealedClaimPdfService {

    private final EmailContentTemplates emailTemplates;
    private final PDFServiceClient pdfServiceClient;
    private final SealedClaimContentProvider sealedClaimContentProvider;

    @Autowired
    public CitizenSealedClaimPdfService(
        final EmailContentTemplates emailTemplates,
        final PDFServiceClient pdfServiceClient,
        final SealedClaimContentProvider sealedClaimContentProvider
    ) {
        this.emailTemplates = emailTemplates;
        this.pdfServiceClient = pdfServiceClient;
        this.sealedClaimContentProvider = sealedClaimContentProvider;
    }

    public byte[] createPdf(final Claim claim, final String submitterEmail) {
        requireNonNull(claim);
        requireNonNull(submitterEmail);

        return pdfServiceClient.generateFromHtml(
            emailTemplates.getSealedClaim(),
            sealedClaimContentProvider.createContent(claim, submitterEmail)
        );
    }
}
