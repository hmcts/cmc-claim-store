package uk.gov.hmcts.cmc.claimstore.services.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.DraftClaimReceiptService;
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

import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.MEDIATION_AGREEMENT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.ORDER_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.ORDER_SANCTIONS;

@Service("documentsService")
public class DocumentManagementBackedDocumentsService implements DocumentsService {

    private final ClaimService claimService;
    private final DocumentManagementService documentManagementService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private final DraftClaimReceiptService draftClaimReceiptService;
    private final ClaimIssueReceiptService claimIssueReceiptService;
    private final DefendantResponseReceiptService defendantResponseReceiptService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;
    private final ReviewOrderService reviewOrderService;
    private final ClaimantDirectionsQuestionnairePdfService claimantDirectionsQuestionnairePdfService;
    private final UserService userService;

    @Autowired
    @SuppressWarnings("squid:S00107")
    // Content providers are formatted values and aren't worth splitting into multiple models.
    public DocumentManagementBackedDocumentsService(
        ClaimService claimService,
        DocumentManagementService documentManagementService,
        DraftClaimReceiptService draftClaimReceiptService,
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
        this.draftClaimReceiptService = draftClaimReceiptService;
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
            case DRAFT_CLAIM_RECEIPT:
                return draftClaimReceiptService;
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
    public byte[] getOCON9xForm(String externalId, String authorisation) {

        User user = userService.getUser(authorisation);
        Claim claim = claimService.getClaimByExternalId(externalId, user);

        ScannedDocument oconDocument = claim.getScannedDocument(ScannedDocumentType.FORM, ScannedDocumentSubtype.OCON9X)
            .orElseThrow(() -> new IllegalArgumentException("Document is not available for download."));

        return documentManagementService.downloadScannedDocument(authorisation, oconDocument);
    }

    @Override
    public byte[] generateDocument(String externalId, ClaimDocumentType claimDocumentType, String authorisation) {
        User user = userService.getUser(authorisation);

        Claim claim = claimService.getClaimByExternalId(externalId, user);
        ClaimDocumentsAccessRule.assertDocumentCanBeAccessedByUser(claim, claimDocumentType, user);

        return processRequest(claim, authorisation, claimDocumentType);
    }

    private byte[] processRequest(Claim claim, String authorisation, ClaimDocumentType claimDocumentType) {

        if (claimDocumentType == ORDER_DIRECTIONS || claimDocumentType == ORDER_SANCTIONS
            || claimDocumentType == MEDIATION_AGREEMENT) {
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
