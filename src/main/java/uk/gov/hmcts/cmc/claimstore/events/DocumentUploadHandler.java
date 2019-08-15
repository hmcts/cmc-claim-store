package uk.gov.hmcts.cmc.claimstore.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.ReviewOrderService;
import uk.gov.hmcts.cmc.claimstore.documents.SettlementAgreementCopyService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.revieworder.ReviewOrderEvent;
import uk.gov.hmcts.cmc.claimstore.events.settlement.CountersignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@Component
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class DocumentUploadHandler {

    private static final Logger logger = LoggerFactory.getLogger(DocumentUploadHandler.class);
    private static final String CLAIM_MUST_NOT_BE_NULL = "Claim must not be null";

    private final DocumentsService documentService;
    private final DefendantResponseReceiptService defendantResponseReceiptService;
    private final SettlementAgreementCopyService settlementAgreementCopyService;
    private final ClaimIssueReceiptService claimIssueReceiptService;
    private final ReviewOrderService reviewOrderService;

    @Autowired
    public DocumentUploadHandler(
        DefendantResponseReceiptService defendantResponseReceiptService,
        SettlementAgreementCopyService settlementAgreementCopyService,
        ClaimIssueReceiptService claimIssueReceiptService,
        DocumentsService documentService,
        ReviewOrderService reviewOrderService) {
        this.defendantResponseReceiptService = defendantResponseReceiptService;
        this.reviewOrderService = reviewOrderService;
        this.settlementAgreementCopyService = settlementAgreementCopyService;
        this.claimIssueReceiptService = claimIssueReceiptService;
        this.documentService = documentService;
    }

    @EventListener
    @LogExecutionTime
    public void uploadCitizenClaimDocument(DocumentGeneratedEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);
        List<PDF> documents = event.getDocuments().stream()
            .filter(document -> document.getClaimDocumentType() == SEALED_CLAIM)
            .collect(Collectors.toList());

        if (!claim.getClaimData().isClaimantRepresented()) {
            documents.add(claimIssueReceiptService.createPdf(claim));
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
        PDF defendantResponseDocument = defendantResponseReceiptService.createPdf(claim);
        uploadToDocumentManagement(claim, event.getAuthorization(), singletonList(defendantResponseDocument));
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
        PDF document = settlementAgreementCopyService.createPdf(claim);
        uploadToDocumentManagement(claim, authorisation, singletonList(document));
    }

    @EventListener
    public void uploadReviewOrderRequestDocument(ReviewOrderEvent event) {
        Claim claim = event.getClaim();
        requireNonNull(claim, CLAIM_MUST_NOT_BE_NULL);

        if (!claim.getReviewOrder().isPresent()) {
            throw new NotFoundException("Review Order does not exist for this claim");
        }

        PDF reviewOrderDocument = reviewOrderService.createPdf(claim);
        uploadToDocumentManagement(claim, event.getAuthorisation(), singletonList(reviewOrderDocument));
    }

    public Claim uploadToDocumentManagement(Claim claim, String authorisation, List<PDF> documents) {
        Claim updatedClaim = claim;
        for (PDF document : documents) {
            try {
                updatedClaim = documentService.uploadToDocumentManagement(document, authorisation, updatedClaim);
            } catch (Exception ex) {
                logger.warn(String.format("unable to upload document %s into document management",
                    document.getFilename()), ex);
            }
        }
        return updatedClaim;
    }
}
