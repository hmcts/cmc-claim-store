package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.*;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.function.Supplier;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;

@Service
@ConditionalOnProperty(prefix = "document_management", name = "api_gateway.url")
public class DocumentManagementBackedDocumentsService implements DocumentsService {

    private final ClaimService claimService;
    private final DocumentManagementService documentManagementService;
    private final PDFServiceClient pdfServiceClient;
    private final CitizenServiceDocumentsService citizenServiceDocumentsService;
    private final ClaimIssueReceiptService claimIssueReceiptService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final DefendantResponseReceiptService defendantResponseReceiptService;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;

    @Autowired
    @SuppressWarnings("squid:S00107")
    // Content providers are formatted values and aren't worth splitting into multiple models.
    public DocumentManagementBackedDocumentsService(
        ClaimService claimService,
        DocumentManagementService documentManagementService,
        PDFServiceClient pdfServiceClient,
        CitizenServiceDocumentsService citizenServiceDocumentsService,
        ClaimIssueReceiptService claimIssueReceiptService,
        LegalSealedClaimPdfService legalSealedClaimPdfService,
        DefendantResponseReceiptService defendantResponseReceiptService,
        CountyCourtJudgmentPdfService countyCourtJudgmentPdfService,
        SettlementAgreementCopyService settlementAgreementCopyService) {
        this.claimService = claimService;
        this.documentManagementService = documentManagementService;
        this.pdfServiceClient = pdfServiceClient;
        this.citizenServiceDocumentsService = citizenServiceDocumentsService;
        this.claimIssueReceiptService = claimIssueReceiptService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
    }

    @Override
    public byte[] generateClaimIssueReceipt(String externalId, String authorisation) {
        return claimIssueReceiptService.createPdf(getClaimByExternalId(externalId, authorisation));
    }

    @Override
    public byte[] getLegalSealedClaim(String externalId, String authorisation) {
        Claim claim = getClaimByExternalId(externalId, authorisation);
        return downloadOrGenerateAndUpload(claim, () -> legalSealedClaimPdfService.createPdf(claim), authorisation);
    }

    @Override
    public byte[] getSealedClaim(String externalId, String authorisation) {
        Claim claim = getClaimByExternalId(externalId, authorisation);
        return downloadOrGenerateAndUpload(claim, () -> {
            Document document = citizenServiceDocumentsService.sealedClaimDocument(claim);
            return pdfServiceClient.generateFromHtml(document.template.getBytes(), document.values);
        }, authorisation);
    }

    @Override
    public byte[] generateDefendantResponseReceipt(String externalId, String authorisation) {
        return defendantResponseReceiptService.createPdf(getClaimByExternalId(externalId, authorisation));
    }

    @Override
    public byte[] generateCountyCourtJudgement(String externalId, String authorisation) {
        return countyCourtJudgmentPdfService.createPdf(getClaimByExternalId(externalId, authorisation));
    }

    @Override
    public byte[] generateSettlementAgreement(String externalId, String authorisation) {
        return settlementAgreementCopyService.createPdf(getClaimByExternalId(externalId, authorisation));
    }

    private Claim getClaimByExternalId(String externalId, String authorisation) {
        return claimService.getClaimByExternalId(externalId, authorisation);
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
