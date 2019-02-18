package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentStore;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.net.URI;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@Qualifier("DocumentManagementBackedDocumentsService")
@Service
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentManagementBackedDocumentsService implements DocumentsService {

    private final ClaimService claimService;
    private final DocumentManagementService documentManagementService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private final ClaimIssueReceiptService claimIssueReceiptService;
    private final DefendantResponseReceiptService defendantResponseReceiptService;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;

    @Autowired
    @SuppressWarnings("squid:S00107")
    // Content providers are formatted values and aren't worth splitting into multiple models.
    public DocumentManagementBackedDocumentsService(
        ClaimService claimService,
        DocumentManagementService documentManagementService,
        SealedClaimPdfService sealedClaimPdfService,
        ClaimIssueReceiptService claimIssueReceiptService,
        DefendantResponseReceiptService defendantResponseReceiptService,
        CountyCourtJudgmentPdfService countyCourtJudgmentPdfService,
        SettlementAgreementCopyService settlementAgreementCopyService) {
        this.claimService = claimService;
        this.documentManagementService = documentManagementService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.claimIssueReceiptService = claimIssueReceiptService;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
    }

    @Override
    public byte[] generateClaimIssueReceipt(String externalId, String authorisation) {
        return claimIssueReceiptService.createPdf(getClaimByExternalId(externalId, authorisation));
    }

    @Override
    public byte[] getSealedClaim(String externalId, String authorisation) {
        Claim claim = getClaimByExternalId(externalId, authorisation);
        final String baseFileName = buildSealedClaimFileBaseName(claim.getReferenceNumber());
        Optional<URI> sealedClaimDocumentUri = claim.getSealedClaimDocument();
        try {
            if (sealedClaimDocumentUri.isPresent()) {
                URI documentSelfPath = sealedClaimDocumentUri.get();
                return documentManagementService.downloadDocument(authorisation, documentSelfPath, baseFileName);
            } else {
                return uploadToDocumentManagement(sealedClaimPdfService.createPdf(claim),
                    authorisation,
                    baseFileName,
                    SEALED_CLAIM,
                    claim);
            }
        } catch (Exception ex) {
            return sealedClaimPdfService.createPdf(claim);
        }
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

    @Override
    public byte[] uploadToDocumentManagement(
        byte[] documentBytes,
        String authorisation,
        String baseFileName,
        ClaimDocumentType claimDocumentType,
        Claim claim) {
        PDF document = new PDF(baseFileName, documentBytes, claimDocumentType);
        URI documentSelfPath = documentManagementService.uploadDocument(authorisation, document);
        claimService.linkClaimToDocument(authorisation,
            claim.getId(),
            getClaimDocumentStore(claim.getExternalId(), document, documentSelfPath, authorisation));
        return documentBytes;
    }

    private ClaimDocumentStore getClaimDocumentStore(String externalId, PDF document, URI uri, String authorisation) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        ClaimDocumentStore claimDocumentStore = claim.getClaimDocumentStore().orElse(new ClaimDocumentStore());
        claimDocumentStore.addClaimDocument(new ClaimDocument(uri,
            document.getFilename(),
            document.getClaimDocumentType(),
            LocalDateTimeFactory.nowInLocalZone(),
            LocalDateTimeFactory.nowInLocalZone(),
            null));
        return claimDocumentStore;
    }
}
