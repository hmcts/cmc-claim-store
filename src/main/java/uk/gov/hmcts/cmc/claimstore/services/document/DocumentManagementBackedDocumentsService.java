package uk.gov.hmcts.cmc.claimstore.services.document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.PdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.CCDEventProducer;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.ORDER_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.ORDER_SANCTIONS;

@Service("documentsService")
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentManagementBackedDocumentsService implements DocumentsService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentManagementBackedDocumentsService.class);

    private static final String OCMC = "OCMC";
    private final ClaimService claimService;
    private final DocumentManagementService documentManagementService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private final ClaimIssueReceiptService claimIssueReceiptService;
    private final DefendantResponseReceiptService defendantResponseReceiptService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;
    private final CCDEventProducer ccdEventProducer;

    @Autowired
    @SuppressWarnings("squid:S00107")
    // Content providers are formatted values and aren't worth splitting into multiple models.
    public DocumentManagementBackedDocumentsService(
        ClaimService claimService,
        DocumentManagementService documentManagementService,
        SealedClaimPdfService sealedClaimPdfService,
        ClaimIssueReceiptService claimIssueReceiptService,
        DefendantResponseReceiptService defendantResponseReceiptService,
        SettlementAgreementCopyService settlementAgreementCopyService,
        CCDEventProducer ccdEventProducer
    ) {
        this.claimService = claimService;
        this.documentManagementService = documentManagementService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.claimIssueReceiptService = claimIssueReceiptService;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
        this.ccdEventProducer = ccdEventProducer;
    }

    private PdfService getService(ClaimDocumentType claimDocumentType) {
        switch (claimDocumentType) {
            case CLAIM_ISSUE_RECEIPT:
                return claimIssueReceiptService;
            case SEALED_CLAIM:
                return sealedClaimPdfService;
            case DEFENDANT_RESPONSE_RECEIPT:
                return defendantResponseReceiptService;
            case SETTLEMENT_AGREEMENT:
                return settlementAgreementCopyService;
            default:
                throw new IllegalArgumentException(
                    "Unknown document service for document of type " + claimDocumentType.name());
        }
    }

    @Override
    public byte[] generateDocument(String externalId, ClaimDocumentType claimDocumentType, String authorisation) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        return processRequest(claim, authorisation, claimDocumentType);
    }

    private byte[] processRequest(Claim claim, String authorisation, ClaimDocumentType claimDocumentType) {

        if (claimDocumentType == ORDER_DIRECTIONS || claimDocumentType == ORDER_SANCTIONS) {
            return getOrderDocuments(claim, authorisation, claimDocumentType);
        } else {
            return getClaimJourneyDocuments(claim, authorisation, claimDocumentType);
        }
    }

    private byte[] getOrderDocuments(Claim claim, String authorisation, ClaimDocumentType claimDocumentType) {
        Optional<ClaimDocument> claimDocument = claim.getClaimDocument(claimDocumentType);
        return claimDocument
            .map(document -> documentManagementService.downloadDocument(authorisation, document))
            .orElseThrow(() -> new IllegalArgumentException("Document is not available for download."));
    }

    private byte[] getClaimJourneyDocuments(Claim claim, String authorisation, ClaimDocumentType claimDocumentType) {
        try {
            Optional<ClaimDocument> claimDocument = claim.getClaimDocument(claimDocumentType);
            return claimDocument
                .map(document -> documentManagementService.downloadDocument(authorisation, document))
                .orElseGet(() -> generateNewDocument(claim, authorisation, claimDocumentType));

        } catch (Exception ex) {
            return getService(claimDocumentType).createPdf(claim).getBytes();
        }
    }

    private byte[] generateNewDocument(Claim claim, String authorisation, ClaimDocumentType claimDocumentType) {
        PDF document = getService(claimDocumentType).createPdf(claim);
        uploadToDocumentManagement(document, authorisation, claim);
        return document.getBytes();
    }

    public Claim uploadToDocumentManagement(PDF document, String authorisation, Claim claim) {
        ClaimDocument claimDocument = documentManagementService.uploadDocument(authorisation, document);
        ClaimDocumentCollection claimDocumentCollection = getClaimDocumentCollection(claim, claimDocument);

        Claim newClaim = claimService.saveClaimDocuments(authorisation,
            claim.getId(),
            claimDocumentCollection,
            document.getClaimDocumentType());

        ccdEventProducer.saveClaimDocumentCCDEvent(authorisation,
            claim,
            claimDocumentCollection,
            document.getClaimDocumentType());

        return newClaim;
    }

    private ClaimDocumentCollection getClaimDocumentCollection(Claim claim, ClaimDocument claimDocument) {
        ClaimDocumentCollection claimDocumentCollection = claim.getClaimDocumentCollection()
            .orElse(new ClaimDocumentCollection());
        claimDocumentCollection.addClaimDocument(claimDocument);
        return claimDocumentCollection;
    }
}
