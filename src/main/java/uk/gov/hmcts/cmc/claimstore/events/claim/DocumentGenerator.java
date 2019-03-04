package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimIssueReceiptService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentUploadEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimIssueReceiptFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@Component
public class DocumentGenerator {

    private final CitizenServiceDocumentsService citizenServiceDocumentsService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private final ApplicationEventPublisher publisher;
    private final PDFServiceClient pdfServiceClient;
    private final ClaimIssueReceiptService claimIssueReceiptService;

    @Autowired
    public DocumentGenerator(
        CitizenServiceDocumentsService citizenServiceDocumentsService,
        SealedClaimPdfService sealedClaimPdfService,
        ApplicationEventPublisher publisher,
        PDFServiceClient pdfServiceClient,
        ClaimIssueReceiptService claimIssueReceiptService
    ) {
        this.citizenServiceDocumentsService = citizenServiceDocumentsService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.publisher = publisher;
        this.pdfServiceClient = pdfServiceClient;
        this.claimIssueReceiptService = claimIssueReceiptService;
    }

    @EventListener
    public void generateForNonRepresentedClaim(CitizenClaimIssuedEvent event) {
        Document sealedClaimDoc = citizenServiceDocumentsService.sealedClaimDocument(event.getClaim());

        PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(event.getClaim().getReferenceNumber()),
            sealedClaimPdfService.createPdf(event.getClaim()), SEALED_CLAIM);

        Document defendantLetterDoc = citizenServiceDocumentsService.pinLetterDocument(event.getClaim(),
            event.getPin());

        PDF defendantLetter = new PDF(buildDefendantLetterFileBaseName(event.getClaim().getReferenceNumber()),
            pdfServiceClient.generateFromHtml(defendantLetterDoc.template.getBytes(), defendantLetterDoc.values),
            DEFENDANT_PIN_LETTER);

        PDF claimIssueReceipt = new PDF(buildClaimIssueReceiptFileBaseName(event.getClaim().getReferenceNumber()),
            claimIssueReceiptService.createPdf(event.getClaim()), ClaimDocumentType.CLAIM_ISSUE_RECEIPT);

        publisher.publishEvent(new DocumentReadyToPrintEvent(event.getClaim(),
            defendantLetterDoc, sealedClaimDoc));
        publisher.publishEvent(new DocumentGeneratedEvent(event.getClaim(), event.getAuthorisation(),
            sealedClaim, defendantLetter, claimIssueReceipt));
        publisher.publishEvent(new DocumentUploadEvent(event.getClaim(), event.getAuthorisation(),
            sealedClaim, defendantLetter, claimIssueReceipt));
    }

    @EventListener
    public void generateForRepresentedClaim(RepresentedClaimIssuedEvent event) {
        PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(event.getClaim().getReferenceNumber()),
            sealedClaimPdfService.createPdf(event.getClaim()), SEALED_CLAIM);

        publisher.publishEvent(new DocumentGeneratedEvent(event.getClaim(), event.getAuthorisation(), sealedClaim));
        publisher.publishEvent(new DocumentUploadEvent(event.getClaim(), event.getAuthorisation(), sealedClaim));
    }

    public void generateForCitizenRPA(CitizenClaimIssuedEvent event) {
        PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(event.getClaim().getReferenceNumber()),
            sealedClaimPdfService.createPdf(event.getClaim()), SEALED_CLAIM);

        publisher.publishEvent(new DocumentGeneratedEvent(event.getClaim(), event.getAuthorisation(),
            sealedClaim));
    }
}
