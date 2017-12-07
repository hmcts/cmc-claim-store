package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "false")
public class AlwaysGenerateDocumentsService implements DocumentsService {

    private final ClaimService claimService;
    private final ClaimIssueReceiptService claimIssueReceiptService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final DefendantResponseCopyService defendantResponseCopyService;
    private final DefendantResponseReceiptService defendantResponseReceiptService;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;
    private final DocumentTemplates documentTemplates;

    @Autowired
    public AlwaysGenerateDocumentsService(
        final ClaimService claimService,
        final ClaimIssueReceiptService claimIssueReceiptService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final DefendantResponseCopyService defendantResponseCopyService,
        final DefendantResponseReceiptService defendantResponseReceiptService,
        final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService,
        final SettlementAgreementCopyService settlementAgreementCopyService,
        final DocumentTemplates documentTemplates) {
        this.claimService = claimService;
        this.claimIssueReceiptService = claimIssueReceiptService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.defendantResponseCopyService = defendantResponseCopyService;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
        this.documentTemplates = documentTemplates;
    }

    @Override
    public byte[] generateClaimIssueReceipt(String externalId) {
        return claimIssueReceiptService.createPdf(getClaimByExternalId(externalId));
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
    public byte[] generateDefendantResponseReceipt(String externalId) {
        return defendantResponseReceiptService.createPdf(getClaimByExternalId(externalId));
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
