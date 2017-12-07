package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.function.Supplier;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;

@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "document_management", havingValue = "true")
public class DocumentManagementBackedDocumentsService implements DocumentsService {

    private final ClaimService claimService;
    private final DocumentManagementService documentManagementService;
    private final ClaimIssueReceiptService claimIssueReceiptService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final DefendantResponseCopyService defendantResponseCopyService;
    private final DefendantResponseReceiptService defendantResponseReceiptService;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;

    @Autowired
    public DocumentManagementBackedDocumentsService(
        final ClaimService claimService,
        final DocumentManagementService documentManagementService,
        final ClaimIssueReceiptService claimIssueReceiptService,
        final LegalSealedClaimPdfService legalSealedClaimPdfService,
        final DefendantResponseCopyService defendantResponseCopyService,
        final DefendantResponseReceiptService defendantResponseReceiptService,
        final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService,
        final SettlementAgreementCopyService settlementAgreementCopyService) {
        this.claimService = claimService;
        this.documentManagementService = documentManagementService;
        this.claimIssueReceiptService = claimIssueReceiptService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.defendantResponseCopyService = defendantResponseCopyService;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
    }

    @Override
    public byte[] generateClaimIssueReceipt(String externalId) {
        return claimIssueReceiptService.createPdf(getClaimByExternalId(externalId));
    }

    @Override
    public byte[] getLegalSealedClaim(final String externalId, final String authorisation) {
        Claim claim = getClaimByExternalId(externalId);
        return downloadOrGenerateAndUpload(claim, () -> legalSealedClaimPdfService.createPdf(claim), authorisation);
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

    @SuppressWarnings("squid:S3655")
    private byte[] downloadOrGenerateAndUpload(Claim claim, Supplier<byte[]> documentSupplier, String authorisation) {
        if (claim.getSealedClaimDocumentSelfPath().isPresent()) {
            String documentSelfPath = claim.getSealedClaimDocumentSelfPath().get();
            return documentManagementService.downloadDocument(authorisation, documentSelfPath);
        } else {
            PDF document = new PDF(buildSealedClaimFileBaseName(claim.getReferenceNumber()), documentSupplier.get());

            String documentSelfPath = documentManagementService.uploadDocument(authorisation, document);
            claimService.linkSealedClaimDocument(claim.getId(), documentSelfPath);

            return document.getBytes();
        }
    }
}
