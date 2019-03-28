package uk.gov.hmcts.cmc.claimstore.services.document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantPinLetterPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.PdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.net.URI;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimIssueReceiptFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForJudgementFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSettlementReachedFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CCJ_REQUEST;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;

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
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;
    private final DefendantPinLetterPdfService defendantPinLetterPdfService;

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
        SettlementAgreementCopyService settlementAgreementCopyService,
        DefendantPinLetterPdfService defendantPinLetterPdfService
    ) {
        this.claimService = claimService;
        this.documentManagementService = documentManagementService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.claimIssueReceiptService = claimIssueReceiptService;
        this.defendantResponseReceiptService = defendantResponseReceiptService;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
        this.defendantPinLetterPdfService = defendantPinLetterPdfService;
    }

    @Override
    public byte[] generateClaimIssueReceipt(String externalId, String authorisation) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        return processRequest(claim,
            authorisation,
            CLAIM_ISSUE_RECEIPT,
            claimIssueReceiptService,
            buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()));
    }

    @Override
    public byte[] generateSealedClaim(String externalId, String authorisation) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        return processRequest(claim,
            authorisation,
            SEALED_CLAIM,
            sealedClaimPdfService,
            buildSealedClaimFileBaseName(claim.getReferenceNumber()));
    }

    @Override
    public byte[] generateDefendantResponseReceipt(String externalId, String authorisation) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        if (!claim.getResponse().isPresent() && null == claim.getRespondedAt()) {
            throw new NotFoundException("Defendant response does not exist for this claim");
        }
        return processRequest(claim,
            authorisation,
            DEFENDANT_RESPONSE_RECEIPT,
            defendantResponseReceiptService,
            buildResponseFileBaseName(claim.getReferenceNumber()));
    }

    @Override
    public byte[] generateCountyCourtJudgement(String externalId, String authorisation) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        if (null == claim.getCountyCourtJudgment() && null == claim.getCountyCourtJudgmentRequestedAt()) {
            throw new NotFoundException("County Court Judgment does not exist for this claim");
        }
        return processRequest(claim,
            authorisation,
            CCJ_REQUEST,
            countyCourtJudgmentPdfService,
            buildRequestForJudgementFileBaseName(claim.getReferenceNumber(),
                claim.getClaimData().getDefendant().getName()));
    }

    @Override
    public byte[] generateSettlementAgreement(String externalId, String authorisation) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        if (!claim.getSettlement().isPresent() && null == claim.getSettlementReachedAt()) {
            throw new NotFoundException("Settlement Agreement does not exist for this claim");
        }
        return processRequest(claim,
            authorisation,
            SETTLEMENT_AGREEMENT,
            settlementAgreementCopyService,
            buildSettlementReachedFileBaseName(claim.getReferenceNumber()));
    }

    @Override
    public void generateDefendantPinLetter(String externalId, String pin, String authorisation) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorisation);
        final String fileName = buildDefendantLetterFileBaseName(claim.getReferenceNumber());
        Optional<URI> claimDocument = claim.getClaimDocument(DEFENDANT_PIN_LETTER);
        if (!claimDocument.isPresent()) {
            try {
                PDF defendantLetter = new PDF(fileName,
                    defendantPinLetterPdfService.createPdf(claim, pin),
                    DEFENDANT_PIN_LETTER);
                uploadToDocumentManagement(defendantLetter, authorisation, claim);
            } catch (Exception ex) {
                logger.warn(String.format("unable to upload document %s into document management",
                    fileName), ex);
            }
        }
    }

    private byte[] processRequest(
        Claim claim,
        String authorisation,
        ClaimDocumentType claimDocumentType,
        PdfService pdfService,
        String baseFileName
    ) {
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

    public Claim uploadToDocumentManagement(
        PDF document,
        String authorisation,
        Claim claim
    ) {
        URI documentSelfPath = documentManagementService.uploadDocument(authorisation, document);
        return claimService.saveClaimDocuments(authorisation,
            claim.getId(),
            getClaimDocumentCollection(claim, document, documentSelfPath),
            document.getClaimDocumentType());
    }

    private ClaimDocumentCollection getClaimDocumentCollection(
        Claim claim,
        PDF document,
        URI uri
    ) {
        ClaimDocumentCollection claimDocumentCollection = claim.getClaimDocumentCollection()
            .orElse(new ClaimDocumentCollection());

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
