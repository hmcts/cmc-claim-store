package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;

@Component
public class DocumentGenerator {

    private final CitizenServiceDocumentsService citizenServiceDocumentsService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private final ApplicationEventPublisher publisher;
    private final PDFServiceClient pdfServiceClient;

    @Autowired
    public DocumentGenerator(
        CitizenServiceDocumentsService citizenServiceDocumentsService,
        SealedClaimPdfService sealedClaimPdfService,
        ApplicationEventPublisher publisher,
        PDFServiceClient pdfServiceClient
    ) {
        this.citizenServiceDocumentsService = citizenServiceDocumentsService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.publisher = publisher;
        this.pdfServiceClient = pdfServiceClient;
    }

    @EventListener
    public void generateForNonRepresentedClaim(CitizenClaimIssuedEvent event) {
        Document sealedClaimDoc = citizenServiceDocumentsService.sealedClaimDocument(event.getClaim());

        PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(event.getClaim().getReferenceNumber()),
            sealedClaimPdfService.createPdf(event.getClaim()));

        Document defendantLetterDoc = citizenServiceDocumentsService.pinLetterDocument(event.getClaim(),
            event.getPin());

        PDF defendantLetter = new PDF(buildDefendantLetterFileBaseName(event.getClaim().getReferenceNumber()),
            pdfServiceClient.generateFromHtml(defendantLetterDoc.template.getBytes(), defendantLetterDoc.values));

        publisher.publishEvent(new DocumentReadyToPrintEvent(event.getClaim(),
            defendantLetterDoc, sealedClaimDoc));
        publisher.publishEvent(new DocumentGeneratedEvent(event.getClaim(), event.getAuthorisation(),
            sealedClaim, defendantLetter));
    }

    @EventListener
    public void generateForRepresentedClaim(RepresentedClaimIssuedEvent event) {
        PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(event.getClaim().getReferenceNumber()),
            sealedClaimPdfService.createPdf(event.getClaim()));

        publisher.publishEvent(new DocumentGeneratedEvent(event.getClaim(), event.getAuthorisation(), sealedClaim));
    }
}
