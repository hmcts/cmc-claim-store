package uk.gov.hmcts.cmc.claimstore.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.CountyCourtJudgmentPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.settlement.CountersignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimIssueReceiptFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildRequestForJudgementFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildResponseFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSettlementReachedFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CCJ_REQUEST;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;

@Component
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentUploadHandler {

    private static final Logger logger = LoggerFactory.getLogger(DocumentUploadHandler.class);
    private static final String CLAIM_MUST_NOT_BE_NULL = "Claim must not be null";

    private final DocumentsService documentService;
    private final DefendantResponseReceiptService defendantResponseReceiptService;
    private final CountyCourtJudgmentPdfService countyCourtJudgmentPdfService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;
    private final ClaimIssueReceiptService claimIssueReceiptService;

    @Autowired
    public DocumentUploadHandler(
        DefendantResponseReceiptService defendantResponseReceiptService,
        CountyCourtJudgmentPdfService countyCourtJudgmentPdfService,
        SettlementAgreementCopyService settlementAgreementCopyService,
        ClaimIssueReceiptService claimIssueReceiptService,
        DocumentsService documentService
    ) {
        this.defendantResponseReceiptService = defendantResponseReceiptService;
        this.countyCourtJudgmentPdfService = countyCourtJudgmentPdfService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
        this.claimIssueReceiptService = claimIssueReceiptService;
        this.documentService = documentService;
    }

    @EventListener
    @LogExecutionTime
    public void uploadCitizenClaimDocument(DocumentGeneratedEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);

        List<PDF> documents = new ArrayList<>(event.getDocuments());

        if (!claim.getClaimData().isClaimantRepresented()) {
            documents.add(
                new PDF(buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()),
                    claimIssueReceiptService.createPdf(claim),
                    CLAIM_ISSUE_RECEIPT
                )
            );
        }

        uploadToDocumentManagement(claim, event.getAuthorisation(), documents);
    }

    @EventListener
    @LogExecutionTime
    public void uploadDefendantResponseDocument(DefendantResponseEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        if (!claim.getResponse().isPresent() && null == claim.getRespondedAt()) {
            throw new NotFoundException("Defendant response does not exist for this claim");
        }
        PDF defendantResponseDocument = new PDF(buildResponseFileBaseName(claim.getReferenceNumber()),
            defendantResponseReceiptService.createPdf(claim),
            DEFENDANT_RESPONSE_RECEIPT);
        uploadToDocumentManagement(claim, event.getAuthorization(), singletonList(defendantResponseDocument));
    }

    @EventListener
    @LogExecutionTime
    public void uploadCountyCourtJudgmentDocument(CountyCourtJudgmentEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        if (null == claim.getCountyCourtJudgment() && null == claim.getCountyCourtJudgmentRequestedAt()) {
            throw new NotFoundException("County Court Judgment does not exist for this claim");
        }
        PDF document = new PDF(buildRequestForJudgementFileBaseName(claim.getReferenceNumber(),
            claim.getClaimData().getDefendant().getName()),
            countyCourtJudgmentPdfService.createPdf(claim),
            CCJ_REQUEST);

        uploadToDocumentManagement(claim, event.getAuthorisation(), singletonList(document));
    }

    @EventListener
    @LogExecutionTime
    public void uploadSettlementAgreementDocument(AgreementCountersignedEvent event) {
        processSettlementAgreementUpload(event.getClaim(), event.getAuthorisation());
    }

    @EventListener
    @LogExecutionTime
    public void uploadSettlementAgreementDocument(CountersignSettlementAgreementEvent event) {
        processSettlementAgreementUpload(event.getClaim(), event.getAuthorisation());
    }

    private void processSettlementAgreementUpload(Claim claim, String authorisation) {
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        if (!claim.getSettlement().isPresent() && null == claim.getSettlementReachedAt()) {
            throw new NotFoundException("Settlement Agreement does not exist for this claim");
        }
        PDF document = new PDF(buildSettlementReachedFileBaseName(claim.getReferenceNumber()),
            settlementAgreementCopyService.createPdf(claim),
            SETTLEMENT_AGREEMENT);
        uploadToDocumentManagement(claim, authorisation, singletonList(document));
    }

    private void uploadToDocumentManagement(Claim claim, String authorisation, List<PDF> documents) {
        Claim updatedClaim = claim;
        for (PDF document : documents) {
            try {
                updatedClaim = documentService.uploadToDocumentManagement(document, authorisation, updatedClaim);
            } catch (Exception ex) {
                logger.warn(String.format("unable to upload document %s into document management",
                    document.getFilename()), ex);
            }
        }

    }
}
