package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "true")
public class SealedClaimToDocumentStoreUploader {
    public static final String PDF_EXTENSION = ".pdf";
    public static final String APPLICATION_PDF = "application/pdf";

    private final DocumentManagementService documentManagementService;
    private final ClaimService claimService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final CitizenSealedClaimPdfService citizenSealedClaimPdfService;

    @Autowired
    public SealedClaimToDocumentStoreUploader(
        final DocumentManagementService documentManagementService,
        final ClaimService claimService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final CitizenSealedClaimPdfService citizenSealedClaimPdfService
    ) {
        this.documentManagementService = documentManagementService;
        this.claimService = claimService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.citizenSealedClaimPdfService = citizenSealedClaimPdfService;
    }

    public void uploadCitizenSealedClaim(final String authorisation, final Claim claim, final String submitterEmail) {
        final byte[] n1FormPdf = citizenSealedClaimPdfService.createPdf(claim, submitterEmail);
        upload(authorisation, claim, n1FormPdf);
    }

    public void uploadRepresentativeSealedClaim(final String authorisation, final Claim claim) {
        final byte[] n1FormPdf = legalSealedClaimPdfService.createPdf(claim);
        upload(authorisation, claim, n1FormPdf);
    }

    private void upload(final String authorisation, final Claim claim, final byte[] n1FormPdf) {
        final String originalFileName = claim.getReferenceNumber() + PDF_EXTENSION;

        final String documentSelfPath = documentManagementService.uploadDocument(
            authorisation, originalFileName, n1FormPdf, APPLICATION_PDF);

        claimService.linkSealedClaimDocument(claim.getId(), documentSelfPath);
    }
}
