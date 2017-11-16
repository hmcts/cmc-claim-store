package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.events.DocumentManagementSealedClaimHandler;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Optional;

@Service
public class DocumentsService {
    private final ClaimService claimService;
    private final DefendantResponseCopyService defendantResponseCopyService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    private final DocumentManagementService documentManagementService;
    private final DocumentManagementSealedClaimHandler documentManagementSealedClaimHandler;
    private final boolean documentManagementFeatureEnabled;

    @Autowired
    public DocumentsService(
        final ClaimService claimService,
        final DefendantResponseCopyService defendantResponseCopyService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService,
        final DocumentManagementService documentManagementService,
        final DocumentManagementSealedClaimHandler documentManagementSealedClaimHandler,
        @Value("${feature_toggles.document_management}") final boolean documentManagementFeatureEnabled
    ) {
        this.claimService = claimService;
        this.defendantResponseCopyService = defendantResponseCopyService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
        this.documentManagementService = documentManagementService;
        this.documentManagementSealedClaimHandler = documentManagementSealedClaimHandler;
        this.documentManagementFeatureEnabled = documentManagementFeatureEnabled;
    }

    public byte[] generateDefendantResponseCopy(final String claimExternalId) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return defendantResponseCopyService.createPdf(claim);
    }

    public byte[] generateLegalSealedClaim(final String claimExternalId, final String authorisation) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return downloadOrUploadAndGenerateLegalN1FormPdfDocument(claim, authorisation);
    }

    private byte[] downloadOrUploadAndGenerateLegalN1FormPdfDocument(final Claim claim, final String authorisation) {
        final Optional<String> n1FormDocumentManagementPath = claim.getSealedClaimDocumentManagementSelfPath();

        if (documentManagementFeatureEnabled && n1FormDocumentManagementPath.isPresent()) {
            return documentManagementService.downloadDocument(authorisation, n1FormDocumentManagementPath.get());
        } else {
            documentManagementSealedClaimHandler.uploadRepresentativeSealedClaimToEvidenceStore(
                new RepresentedClaimIssuedEvent(claim, Optional.empty(), authorisation)
            );

            return legalSealedClaimPdfService.createPdf(claim);
        }
    }

    public byte[] generateCountyCourtJudgement(final String claimExternalId) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return countyCourtJudgmentPdfService.createPdf(claim);
    }
}
