package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "true")
public class DocumentManagementSealedClaimDocumentService implements SealedClaimDocumentService {
    private final ClaimService claimService;
    private final DocumentManagementService documentManagementService;

    private final SealedClaimToDocumentStoreUploader sealedClaimToDocumentStoreUploader;

    @Autowired
    public DocumentManagementSealedClaimDocumentService(
        final ClaimService claimService,
        final DocumentManagementService documentManagementService,
        final SealedClaimToDocumentStoreUploader sealedClaimToDocumentStoreUploader
    ) {
        this.claimService = claimService;
        this.documentManagementService = documentManagementService;
        this.sealedClaimToDocumentStoreUploader = sealedClaimToDocumentStoreUploader;
    }

    @Override
    public byte[] generateLegalDocument(final String claimExternalId, final String authorisation) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return downloadOrUploadAndGenerateLegalN1FormPdfDocument(claim, authorisation, null);
    }

    @Override
    public byte[] generateCitizenDocument(
        final String claimExternalId,
        final String authorisation,
        final String submitterEmail
    ) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return downloadOrUploadAndGenerateLegalN1FormPdfDocument(claim, authorisation, submitterEmail);
    }

    private byte[] downloadOrUploadAndGenerateLegalN1FormPdfDocument(
        final Claim claim,
        final String authorisation,
        final String submitterEmail
    ) {
        final Optional<String> documentSelfPath = claim.getDocumentSelfPath();

        if (documentSelfPath.isPresent()) {
            return documentManagementService.downloadDocument(authorisation, documentSelfPath.get());
        } else {
            uploadClaim(claim, authorisation, submitterEmail);

            final Claim updatedClaim = claimService.getClaimByExternalId(claim.getExternalId());

            final String documentSelfLink = updatedClaim.getDocumentSelfPath()
                .orElseThrow(() -> new DocumentManagementException("failed linking documentManagement"));

            return documentManagementService.downloadDocument(authorisation, documentSelfLink);
        }
    }

    private void uploadClaim(final Claim claim, final String authorisation, final String submitterEmail) {
        if (claim.getClaimData().isClaimantRepresented()) {
            sealedClaimToDocumentStoreUploader.uploadRepresentativeSealedClaim(authorisation, claim);
        } else {
            sealedClaimToDocumentStoreUploader.uploadCitizenSealedClaim(authorisation, claim, submitterEmail);
        }
    }
}
