package uk.gov.hmcts.cmc.claimstore.events.claim;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentUploadHandler;
import uk.gov.hmcts.cmc.claimstore.rpa.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
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
public class ClaimIssueOperationHandler {

    private final CitizenServiceDocumentsService citizenServiceDocumentsService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private final PDFServiceClient pdfServiceClient;
    private final ClaimIssueReceiptService claimIssueReceiptService;
    private final DocumentUploadHandler documentUploadHandler;
    private final ClaimIssuedCitizenActionsHandler claimIssuedCitizenActionsHandler;
    private final ClaimIssuedNotificationService rpaNotificationHandler;
    private final BulkPrintService bulkPrintService;
    private final ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;

    @Autowired
    public ClaimIssueOperationHandler(
        CitizenServiceDocumentsService citizenServiceDocumentsService,
        SealedClaimPdfService sealedClaimPdfService,
        PDFServiceClient pdfServiceClient,
        ClaimIssueReceiptService claimIssueReceiptService,
        DocumentUploadHandler documentUploadHandler,
        ClaimIssuedCitizenActionsHandler claimIssuedCitizenActionsHandler,
        @Qualifier("${rpa/claim-issued-notification-service}") ClaimIssuedNotificationService rpaNotificationHandler,
        BulkPrintService bulkPrintService,
        ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService
    ) {
        this.citizenServiceDocumentsService = citizenServiceDocumentsService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.pdfServiceClient = pdfServiceClient;
        this.claimIssueReceiptService = claimIssueReceiptService;
        this.documentUploadHandler = documentUploadHandler;

        this.claimIssuedCitizenActionsHandler = claimIssuedCitizenActionsHandler;
        this.rpaNotificationHandler = rpaNotificationHandler;
        this.bulkPrintService = bulkPrintService;
        this.claimIssuedStaffNotificationService = claimIssuedStaffNotificationService;
    }

    @EventListener
    public void operationHandler(CitizenClaimIssuedEvent event) {
        try {
            Document sealedClaimDoc = citizenServiceDocumentsService.sealedClaimDocument(event.getClaim());
            Document defendantLetterDoc = citizenServiceDocumentsService.pinLetterDocument(event.getClaim(),
                event.getPin());

            PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(event.getClaim().getReferenceNumber()),
                sealedClaimPdfService.createPdf(event.getClaim()), SEALED_CLAIM);
            PDF defendantLetter = new PDF(buildDefendantLetterFileBaseName(event.getClaim().getReferenceNumber()),
                pdfServiceClient.generateFromHtml(defendantLetterDoc.template.getBytes(), defendantLetterDoc.values),
                DEFENDANT_PIN_LETTER);

            PDF claimIssueReceipt = new PDF(buildClaimIssueReceiptFileBaseName(event.getClaim().getReferenceNumber()),
                claimIssueReceiptService.createPdf(event.getClaim()),
                CLAIM_ISSUE_RECEIPT
            );

            Claim claim = documentUploadHandler.uploadToDocumentManagement(
                event.getClaim(),
                event.getAuthorisation(),
                ImmutableList.of(sealedClaim, defendantLetter, claimIssueReceipt)
            );

            claimIssuedCitizenActionsHandler.sendDefendantNotification(event.toBuilder().claim(claim).build());

            bulkPrintService.print(new DocumentReadyToPrintEvent(claim, defendantLetterDoc, sealedClaimDoc));

            rpaNotificationHandler.notifyRobotOfClaimIssue(
                new DocumentGeneratedEvent(claim, event.getAuthorisation(), sealedClaim)
            );

            claimIssuedStaffNotificationService.notifyStaffOfClaimIssue(
                new DocumentGeneratedEvent(claim, event.getAuthorisation(), sealedClaim, defendantLetter)
            );

            claimIssuedCitizenActionsHandler.sendClaimantNotification(event.toBuilder().claim(claim).build());
        } catch (Exception e){

        }
    }
}
