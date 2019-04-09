package uk.gov.hmcts.cmc.claimstore.events.claim;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.operations.BulkPrintOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.ClaimantOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.DefendantOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.NotifyStaffOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RepresentativeOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.RpaOperationService;
import uk.gov.hmcts.cmc.claimstore.events.operations.UploadOperationService;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimIssueReceiptFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@Async("threadPoolTaskExecutor")
@Service
@ConditionalOnProperty(prefix = "feature_toggles", name = "async_eventOperations_enabled")
public class ClaimCreatedOperationHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClaimCreatedOperationHandler.class);

    private final CitizenServiceDocumentsService citizenServiceDocumentsService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private final PDFServiceClient pdfServiceClient;
    private final ClaimIssueReceiptService claimIssueReceiptService;
    private final RepresentativeOperationService representativeOperationService;
    private final BulkPrintOperationService bulkPrintOperationService;
    private final ClaimantOperationService claimantOperationService;
    private final DefendantOperationService defendantOperationService;
    private final RpaOperationService rpaOperationService;
    private final NotifyStaffOperationService staffOperationHandler;
    private final UploadOperationService uploadOperationService;

    @Autowired
    @SuppressWarnings("squid:S00107")
    public ClaimCreatedOperationHandler(
        CitizenServiceDocumentsService citizenServiceDocumentsService,
        SealedClaimPdfService sealedClaimPdfService,
        PDFServiceClient pdfServiceClient,
        ClaimIssueReceiptService claimIssueReceiptService,
        RepresentativeOperationService representativeOperationService,
        BulkPrintOperationService bulkPrintOperationService,
        ClaimantOperationService claimantOperationService,
        DefendantOperationService defendantOperationService,
        RpaOperationService rpaOperationService,
        NotifyStaffOperationService staffOperationHandler,
        UploadOperationService uploadOperationService
    ) {
        this.citizenServiceDocumentsService = citizenServiceDocumentsService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.pdfServiceClient = pdfServiceClient;
        this.claimIssueReceiptService = claimIssueReceiptService;

        this.representativeOperationService = representativeOperationService;
        this.bulkPrintOperationService = bulkPrintOperationService;
        this.claimantOperationService = claimantOperationService;
        this.defendantOperationService = defendantOperationService;
        this.rpaOperationService = rpaOperationService;
        this.staffOperationHandler = staffOperationHandler;
        this.uploadOperationService = uploadOperationService;
    }

    @EventListener
    public void citizenIssueHandler(CitizenClaimCreatedEvent event) {
        try {
            Claim claim = event.getClaim();
            String pin = event.getPin();
            Document sealedClaimDoc = citizenServiceDocumentsService.sealedClaimDocument(claim);
            Document defendantLetterDoc = citizenServiceDocumentsService.pinLetterDocument(claim, pin);

            PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(claim.getReferenceNumber()),
                sealedClaimPdfService.createPdf(claim), SEALED_CLAIM);

            PDF defendantLetter = new PDF(buildDefendantLetterFileBaseName(claim.getReferenceNumber()),
                pdfServiceClient.generateFromHtml(defendantLetterDoc.template.getBytes(), defendantLetterDoc.values),
                DEFENDANT_PIN_LETTER);

            PDF claimIssueReceipt = new PDF(buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()),
                claimIssueReceiptService.createPdf(claim),
                CLAIM_ISSUE_RECEIPT
            );

            String authorisation = event.getAuthorisation();

            Claim updatedClaim = uploadOperationService.uploadDocuments(
                claim,
                authorisation,
                ImmutableList.of(defendantLetter)
            );

            String submitterName = event.getSubmitterName();
            updatedClaim = defendantOperationService.notify(updatedClaim, pin, submitterName, authorisation);
            updatedClaim = bulkPrintOperationService.print(updatedClaim, defendantLetterDoc, sealedClaimDoc);
            updatedClaim = staffOperationHandler.notify(updatedClaim, authorisation, sealedClaim, defendantLetter);

            //TODO Check if above operation indicators are successful, if no return else  continue

            updatedClaim = uploadOperationService.uploadDocuments(
                updatedClaim,
                authorisation,
                ImmutableList.of(sealedClaim, claimIssueReceipt)
            );
            updatedClaim = rpaOperationService.notify(updatedClaim, authorisation, sealedClaim);
            claimantOperationService.notify(updatedClaim, pin, submitterName, authorisation);

            //TODO update claim state
            //claimService.updateState

        } catch (Exception e) {
            logger.error("failed operation processing for event ()", event, e);
        }
    }

    @EventListener
    public void representativeIssueHandler(RepresentedClaimCreatedEvent event) {
        try {
            Claim claim = event.getClaim();
            String authorisation = event.getAuthorisation();

            PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(claim.getReferenceNumber()),
                sealedClaimPdfService.createPdf(claim), SEALED_CLAIM);

            Claim updatedClaim = uploadOperationService
                .uploadDocuments(claim, authorisation, ImmutableList.of(sealedClaim));

            updatedClaim = rpaOperationService.notify(updatedClaim, authorisation, sealedClaim);
            updatedClaim = staffOperationHandler.notify(updatedClaim, authorisation, sealedClaim);

            String submitterName = event.getRepresentativeName().orElse(null);
            representativeOperationService.notify(updatedClaim, submitterName, authorisation);

            //TODO update claim state
            //claimService.updateState

        } catch (Exception e) {
            logger.error("failed operation processing for event ()", event, e);
        }
    }
}
