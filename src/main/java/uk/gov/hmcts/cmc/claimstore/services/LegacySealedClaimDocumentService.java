package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "false")
public class LegacySealedClaimDocumentService implements SealedClaimDocumentService {
    private final ClaimService claimService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final CitizenSealedClaimPdfService citizenSealedClaimPdfService;

    @Autowired
    public LegacySealedClaimDocumentService(
        final ClaimService claimService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final CitizenSealedClaimPdfService citizenSealedClaimPdfService
    ) {
        this.claimService = claimService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.citizenSealedClaimPdfService = citizenSealedClaimPdfService;
    }

    @Override
    public byte[] generateLegalSealedClaim(final String claimExternalId, final String authorisation) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return legalSealedClaimPdfService.createPdf(claim);
    }

    @Override
    public byte[] generateCitizenSealedClaim(final String claimExternalId, final String authorisation,
                                             final String submitterEmail) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return citizenSealedClaimPdfService.createPdf(claim, submitterEmail);
    }
}
