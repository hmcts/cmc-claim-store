package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

@Service("documentsService")
@ConditionalOnProperty(prefix = "document_management", name = "url", havingValue = "false")
public class AlwaysGenerateDocumentsService implements DocumentsService {

    private final ClaimService claimService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private final ClaimIssueReceiptService claimIssueReceiptService;
    private final DefendantResponseReceiptService defendantResponseReceiptService;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;

    @Autowired
    public AlwaysGenerateDocumentsService(
        ClaimService claimService,
        SealedClaimPdfService sealedClaimPdfService,
        ClaimIssueReceiptService claimIssueReceiptService,
        DefendantResponseReceiptService defendantResponseReceiptService,
        CountyCourtJudgmentPdfService countyCourtJudgmentPdfService,
        SettlementAgreementCopyService settlementAgreementCopyService
    ) {
        this.claimService = claimService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.claimIssueReceiptService = claimIssueReceiptService;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
    }

    @Override
    public byte[] generateDocument(String externalId, ClaimDocumentType claimDocumentType, String authorisation) {
        switch (claimDocumentType) {
            case CLAIM_ISSUE_RECEIPT:
                return claimIssueReceiptService.createPdf(
                    getClaimByExternalId(externalId, authorisation)).getBytes();
            case SEALED_CLAIM:
                return sealedClaimPdfService.createPdf(
                    getClaimByExternalId(externalId, authorisation)).getBytes();
            case DEFENDANT_RESPONSE_RECEIPT:
                return defendantResponseReceiptService.createPdf(
                    getClaimByExternalId(externalId, authorisation)).getBytes();
            case SETTLEMENT_AGREEMENT:
                return settlementAgreementCopyService.createPdf(
                    getClaimByExternalId(externalId, authorisation)).getBytes();
            default:
                throw new IllegalArgumentException(
                    "Unknown document service for document of type " + claimDocumentType.name());
        }
    }

    @Override
    public Claim uploadToDocumentManagement(PDF document, String authorisation, Claim claim) {
        throw new UnsupportedOperationException(
            "This method is not supported when Document Management is turned off");
    }

    private Claim getClaimByExternalId(String externalId, String authorisation) {
        return claimService.getClaimByExternalId(externalId, authorisation);
    }
}
