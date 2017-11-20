package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Service
public class SealedClaimDocumentService {
    private final ClaimService claimService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final CitizenSealedClaimPdfService citizenSealedClaimPdfService;

    @Autowired
    public SealedClaimDocumentService(
        final ClaimService claimService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final CitizenSealedClaimPdfService citizenSealedClaimPdfService
    ) {
        this.claimService = claimService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.citizenSealedClaimPdfService = citizenSealedClaimPdfService;
    }

    public byte[] generateLegalSealedClaim(final String claimExternalId, final String authorisation) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return legalSealedClaimPdfService.createPdf(claim);
    }

    public byte[] generateCitizenSealedClaim(final String claimExternalId, final String authorisation,
                                             final String submitterEmail) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return citizenSealedClaimPdfService.createPdf(claim, submitterEmail);
    }
}
