package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.events.DocumentManagementSealedClaimHandler;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "true")
public class DocumentManagementSealedClaimDocumentService implements SealedClaimDocumentService {
    private final ClaimService claimService;
    private final DocumentManagementService documentManagementService;
    private final DocumentManagementSealedClaimHandler documentManagementSealedClaimHandler;

    @Autowired
    public DocumentManagementSealedClaimDocumentService(
        final ClaimService claimService,
        final DocumentManagementService documentManagementService,
        final DocumentManagementSealedClaimHandler documentManagementSealedClaimHandler
    ) {
        this.claimService = claimService;
        this.documentManagementService = documentManagementService;
        this.documentManagementSealedClaimHandler = documentManagementSealedClaimHandler;
    }

    @Override
    public byte[] generateLegalSealedClaim(final String claimExternalId, final String authorisation) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return downloadOrUploadAndGenerateLegalN1FormPdfDocument(claim, authorisation);
    }

    @Override
    public byte[] generateCitizenSealedClaim(
        final String claimExternalId,
        final String authorisation,
        final String submitterEmail
    ) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return downloadOrUploadAndGenerateLegalN1FormPdfDocument(claim, authorisation);
    }

    private byte[] downloadOrUploadAndGenerateLegalN1FormPdfDocument(final Claim claim, final String authorisation) {
        final Optional<String> n1FormDocumentManagementPath = claim.getSealedClaimDocumentManagementSelfPath();

        if (n1FormDocumentManagementPath.isPresent()) {
            return documentManagementService.downloadDocument(authorisation, n1FormDocumentManagementPath.get());
        } else {
            uploadClaim(claim, authorisation);

            final Claim updatedClaim = claimService.getClaimByExternalId(claim.getExternalId());

            final String documentSelfLink = updatedClaim.getSealedClaimDocumentManagementSelfPath()
                .orElseThrow(() -> new DocumentManagementException("failed linking documentManagement"));

            return documentManagementService.downloadDocument(authorisation, documentSelfLink);
        }
    }

    private void uploadClaim(final Claim claim, final String authorisation) {
        if (claim.getClaimData().isClaimantRepresented()) {
            documentManagementSealedClaimHandler.uploadRepresentativeSealedClaimToEvidenceStore(
                new RepresentedClaimIssuedEvent(claim, null, authorisation)
            );
        } else {
            documentManagementSealedClaimHandler.uploadCitizenSealedClaimToEvidenceStore(
                new ClaimIssuedEvent(claim, null, null, authorisation)
            );
        }
    }
}
