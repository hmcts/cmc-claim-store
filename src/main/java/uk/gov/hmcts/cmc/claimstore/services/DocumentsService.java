package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Service
public class DocumentsService {
    private final SealedClaimDocumentService sealedClaimDocumentService;
    private final ClaimService claimService;
    private final DefendantResponseCopyService defendantResponseCopyService;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;

    @Autowired
    public DocumentsService(
        final SealedClaimDocumentService sealedClaimDocumentService,
        final ClaimService claimService,
        final DefendantResponseCopyService defendantResponseCopyService,
        final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService,
        final SettlementAgreementCopyService settlementAgreementCopyService
    ) {

        this.sealedClaimDocumentService = sealedClaimDocumentService;
        this.claimService = claimService;
        this.defendantResponseCopyService = defendantResponseCopyService;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
    }

    public byte[] generateDefendantResponseCopy(final String claimExternalId) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return defendantResponseCopyService.createPdf(claim);
    }

    public byte[] generateLegalSealedClaim(final String claimExternalId, final String authorisation) {
        return sealedClaimDocumentService.generateLegalSealedClaim(claimExternalId, authorisation);
    }

    public byte[] generateCountyCourtJudgement(final String claimExternalId) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return countyCourtJudgmentPdfService.createPdf(claim);
    }

    public byte[] generateSettlementAgreement(final String claimExternalId) {
        final Claim claim = claimService.getClaimByExternalId(claimExternalId);
        return settlementAgreementCopyService.createPdf(claim);
    }
}
