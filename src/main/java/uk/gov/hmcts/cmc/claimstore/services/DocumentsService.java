package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Service
public class DocumentsService {
    private final ClaimService claimService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final DefendantResponseCopyService defendantResponseCopyService;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;

    @Autowired
    public DocumentsService(
        final ClaimService claimService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final DefendantResponseCopyService defendantResponseCopyService,
        final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService,
        final SettlementAgreementCopyService settlementAgreementCopyService
    ) {
        this.claimService = claimService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.defendantResponseCopyService = defendantResponseCopyService;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
    }

    public byte[] generateDefendantResponseCopy(final String claimExternalId) {
        return defendantResponseCopyService.createPdf(getClaimByExternalId(claimExternalId));
    }

    public byte[] generateLegalSealedClaim(final String claimExternalId) {
        return legalSealedClaimPdfService.createPdf(getClaimByExternalId(claimExternalId));
    }

    public byte[] generateCountyCourtJudgement(final String claimExternalId) {
        return countyCourtJudgmentPdfService.createPdf(getClaimByExternalId(claimExternalId));
    }

    public byte[] generateSettlementAgreement(final String claimExternalId) {
        return settlementAgreementCopyService.createPdf(getClaimByExternalId(claimExternalId));
    }

    private Claim getClaimByExternalId(String claimExternalId) {
        return claimService.getClaimByExternalId(claimExternalId);
    }
}
