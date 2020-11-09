package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.PdfService;
import uk.gov.hmcts.cmc.claimstore.documents.ReviewOrderService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.documents.questionnaire.ClaimantDirectionsQuestionnairePdfService;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimDocumentsAccessRule;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentSubtype;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentType;

import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.MEDIATION_AGREEMENT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.ORDER_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.ORDER_SANCTIONS;

@Service("documentsService")
public class DocumentManagementBackedDocumentsService implements DocumentsService {

    private final ClaimService claimService;
    private final DocumentManagementService documentManagementService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private final ClaimIssueReceiptService claimIssueReceiptService;
    private final DefendantResponseReceiptService defendantResponseReceiptService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;
    private final ReviewOrderService reviewOrderService;
    private final ClaimantDirectionsQuestionnairePdfService claimantDirectionsQuestionnairePdfService;
    private final UserService userService;

    public static final String DOCUMENT_IS_NOT_AVAILABLE_FOR_DOWNLOAD = "Document is not available for download.";

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
        ReviewOrderService reviewOrderService,
        ClaimantDirectionsQuestionnairePdfService claimantDirectionsQuestionnairePdfService,
        UserService userService
    ) {
        this.claimService = claimService;
        this.documentManagementService = documentManagementService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.claimIssueReceiptService = claimIssueReceiptService;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
        this.reviewOrderService = reviewOrderService;
        this.claimantDirectionsQuestionnairePdfService = claimantDirectionsQuestionnairePdfService;
        this.userService = userService;
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
            case CLAIMANT_DIRECTIONS_QUESTIONNAIRE:
                return claimantDirectionsQuestionnairePdfService;
            case REVIEW_ORDER:
                return reviewOrderService;
            default:
                throw new IllegalArgumentException(
                    "Unknown document service for document of type " + claimDocumentType.name());
        }
    }

    @Override
    public byte[] generateScannedDocument(String externalId, ScannedDocumentType scannedDocumentType,
                                          ScannedDocumentSubtype scannedDocumentSubtype, String authorisation) {
        User user = userService.getUser(authorisation);
        Claim claim = claimService.getClaimByExternalId(externalId, user);

        ScannedDocument oconDocument = claim.getScannedDocument(scannedDocumentType, scannedDocumentSubtype)
            .orElseThrow(() -> new IllegalArgumentException(DOCUMENT_IS_NOT_AVAILABLE_FOR_DOWNLOAD));

        return documentManagementService.downloadScannedDocument(authorisation, oconDocument);
    }

    @Override
    public byte[] generateDocument(String externalId, ClaimDocumentType claimDocumentType, String authorisation) {
        return generateDocument(externalId, claimDocumentType, null, authorisation);
    }

    @Override
    public byte[] generateDocument(String externalId, ClaimDocumentType claimDocumentType,
                                   String claimDocumentId, String authorisation) {
        User user = userService.getUser(authorisation);

        Claim claim = claimService.getClaimByExternalId(externalId, user);
        ClaimDocumentsAccessRule.assertDocumentCanBeAccessedByUser(claim, claimDocumentType, user);

        if (claimDocumentType == ORDER_DIRECTIONS || claimDocumentType == ORDER_SANCTIONS
            || claimDocumentType == MEDIATION_AGREEMENT) {
            return getOrderDocuments(claim, authorisation, claimDocumentType);
        } else if (claimDocumentType == GENERAL_LETTER) {
            return getGeneralLetters(claim, authorisation, claimDocumentId);
        } else {
            return getClaimJourneyDocuments(claim, authorisation, claimDocumentType);
        }
    }

    private byte[] getGeneralLetters(Claim claim, String authorisation, String claimDocumentId) {
        Optional<ClaimDocument> claimDocument = claim.getClaimDocument(claimDocumentId);
        return claimDocument
            .map(document -> documentManagementService.downloadDocument(authorisation, document))
            .orElseThrow(() -> new IllegalArgumentException(DOCUMENT_IS_NOT_AVAILABLE_FOR_DOWNLOAD));
    }

    private byte[] getOrderDocuments(Claim claim, String authorisation, ClaimDocumentType claimDocumentType) {
        Optional<ClaimDocument> claimDocument = claim.getClaimDocument(claimDocumentType);
        return claimDocument
            .map(document -> documentManagementService.downloadDocument(authorisation, document))
            .orElseThrow(() -> new IllegalArgumentException(DOCUMENT_IS_NOT_AVAILABLE_FOR_DOWNLOAD));
    }

    private byte[] getClaimJourneyDocuments(Claim claim, String authorisation, ClaimDocumentType claimDocumentType) {
        try {
            Optional<ClaimDocument> claimDocument = claim.getClaimDocument(claimDocumentType);
            if (claimDocument.isPresent()) {
                return claimDocument
                    .map(document -> documentManagementService.downloadDocument(authorisation, document))
                    .orElseGet(() -> generateNewDocument(claim, authorisation, claimDocumentType));
            }

            return generateNewDocument(claim, authorisation, claimDocumentType);
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

        return claimService.saveClaimDocuments(authorisation,
            claim.getId(),
            claimDocumentCollection,
            document.getClaimDocumentType());
    }

    private ClaimDocumentCollection getClaimDocumentCollection(Claim claim, ClaimDocument claimDocument) {
        ClaimDocumentCollection claimDocumentCollection = claim.getClaimDocumentCollection()
            .orElse(new ClaimDocumentCollection());
        claimDocumentCollection.addClaimDocument(claimDocument);
        return claimDocumentCollection;
    }
}
