package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.cmc.claimstore.services.DocumentManagementService.PDF_EXTENSION;

@Service
public class DocumentsService {
    public static final String APPLICATION_PDF = "application/pdf";
    private final ClaimService claimService;
    private final DefendantResponseCopyService defendantResponseCopyService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final CountyCourtJudgmentService countyCourtJudgmentService;
    private final DocumentManagementService documentManagementService;
    private final boolean documentManagementFeatureEnabled;

    @Autowired
    public DocumentsService(
        final ClaimService claimService,
        final DefendantResponseCopyService defendantResponseCopyService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final CountyCourtJudgmentService countyCourtJudgmentService,
        final DocumentManagementService documentManagementService,
        @Value("${feature_toggles.document_management}") final boolean documentManagementFeatureEnabled
    ) {
        this.claimService = claimService;
        this.defendantResponseCopyService = defendantResponseCopyService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.countyCourtJudgmentService = countyCourtJudgmentService;
        this.documentManagementService = documentManagementService;
        this.documentManagementFeatureEnabled = documentManagementFeatureEnabled;
    }

    public byte[] defendantResponseCopy(final String claimExternalId) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return defendantResponseCopyService.createPdf(claim);
    }

    public byte[] legalSealedClaim(final String claimExternalId, final String authorisation) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return getLegalN1FormPdfDocument(claim, authorisation);
    }

    private byte[] getLegalN1FormPdfDocument(final Claim claim, final String authorisation) {
        final byte[] n1ClaimPdf = legalSealedClaimPdfService.createPdf(claim);

        if (documentManagementFeatureEnabled) {
            final String n1FormDocumentManagementPath = claim.getSealedClaimDocumentManagementSelfPath();

            if (isBlank(n1FormDocumentManagementPath)) {
                final String originalFileName = claim.getReferenceNumber() + PDF_EXTENSION;

                final String documentManagementSelfPath = documentManagementService.uploadSingleDocument(authorisation,
                    originalFileName, n1ClaimPdf, APPLICATION_PDF);

                claimService.linkDocumentManagement(claim.getId(), documentManagementSelfPath);
            } else {
                return documentManagementService.downloadDocument(authorisation, n1FormDocumentManagementPath);
            }
        }

        return n1ClaimPdf;
    }

    public byte[] countyCourtJudgement(final String claimExternalId) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return countyCourtJudgmentService.createPdf(claim);
    }
}
