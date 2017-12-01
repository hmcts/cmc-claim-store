package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "false")
public class AlwaysGenerateDocumentsService implements DocumentsService {

    private final ClaimService claimService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final DefendantResponseCopyService defendantResponseCopyService;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;

    @Autowired
    public AlwaysGenerateDocumentsService(
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

    @Override
    public byte[] getLegalSealedClaim(final String externalId, final String authorisation) {
        return legalSealedClaimPdfService.createPdf(getClaimByExternalId(externalId));
    }

    @Override
    public byte[] generateDefendantResponseCopy(final String externalId) {
        return defendantResponseCopyService.createPdf(getClaimByExternalId(externalId));
    }

    @Override
    public byte[] generateCountyCourtJudgement(final String externalId) {
        return countyCourtJudgmentPdfService.createPdf(getClaimByExternalId(externalId));
    }

    @Override
    public byte[] generateSettlementAgreement(final String externalId) {
        return settlementAgreementCopyService.createPdf(getClaimByExternalId(externalId));
    }

    private Claim getClaimByExternalId(final String externalId) {
        return claimService.getClaimByExternalId(externalId);
    }
}
