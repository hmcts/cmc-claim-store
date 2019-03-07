package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.PdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.net.URI;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimIssueReceiptFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@Service("documentsService")
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentManagementBackedDocumentsService implements DocumentsService {

    private static final String OCMC = "OCMC";
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
        Claim claim = getClaimByExternalId(externalId, authorisation);
        return processRequest(claim,
            authorisation,
            CLAIM_ISSUE_RECEIPT,
            claimIssueReceiptService,
            buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()));
    }

    @Override
    public byte[] getSealedClaim(String externalId, String authorisation) {
        Claim claim = getClaimByExternalId(externalId, authorisation);
        return processRequest(claim,
            authorisation,
            SEALED_CLAIM,
            sealedClaimPdfService,
            buildSealedClaimFileBaseName(claim.getReferenceNumber()));
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

    private byte[] processRequest(Claim claim,
                                  String authorisation,
                                  ClaimDocumentType claimDocumentType,
                                  PdfService pdfService,
                                  String baseFileName) {
        Optional<URI> claimDocument = claim.getClaimDocument(claimDocumentType);
        try {
            if (claimDocument.isPresent()) {
                URI documentSelfPath = claimDocument.get();
                return documentManagementService.downloadDocument(authorisation, documentSelfPath, baseFileName);
            } else {
                PDF document = new PDF(baseFileName, pdfService.createPdf(claim), claimDocumentType);
                uploadToDocumentManagement(document,
                    authorisation,
                    claim);
                return document.getBytes();
            }
        } catch (Exception ex) {
            return pdfService.createPdf(claim);
        }
    }

    @Override
    public void uploadToDocumentManagement(
        PDF document,
        String authorisation,
        Claim claim) {
        URI documentSelfPath = documentManagementService.uploadDocument(authorisation, document);
        claimService.linkClaimToDocument(authorisation,
            claim.getId(),
            getClaimDocumentStore(claim.getExternalId(), document, documentSelfPath, authorisation));
    }

    private ClaimDocumentCollection getClaimDocumentStore(String externalId,
                                                          PDF document, URI uri,
                                                          String authorisation) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);

        ClaimDocumentCollection claimDocumentCollection;
        if (claim.getClaimData().getDocumentCollection().isPresent()) {
             claimDocumentCollection = claim.getClaimData().getDocumentCollection().get();
        } else {
            claimDocumentCollection = new ClaimDocumentCollection();
        }

        claimDocumentCollection.addClaimDocument(ClaimDocument.builder()
            .documentManagementUrl(uri)
            .documentName(document.getFilename())
            .documentType(document.getClaimDocumentType())
            .createdDatetime(LocalDateTimeFactory.nowInLocalZone())
            .createdBy(OCMC)
            .build());
        return claimDocumentCollection;
    }
}
