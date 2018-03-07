package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.LegalSealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildSealedClaimFileBaseName;

@Component
public class DocumentGenerator {

    private final CitizenServiceDocumentsService citizenServiceDocumentsService;
    private final LegalSealedClaimPdfService legalSealedClaimPdfService;
    private final ApplicationEventPublisher publisher;
    private final PDFServiceClient pdfServiceClient;

    @Autowired
    public DocumentGenerator(
        CitizenServiceDocumentsService citizenServiceDocumentsService,
        LegalSealedClaimPdfService legalSealedClaimPdfService,
        ApplicationEventPublisher publisher,
        PDFServiceClient pdfServiceClient
    ) {
        this.citizenServiceDocumentsService = citizenServiceDocumentsService;
        this.legalSealedClaimPdfService = legalSealedClaimPdfService;
        this.publisher = publisher;
        this.pdfServiceClient = pdfServiceClient;
    }

    @EventListener
    public void generateForNonRepresentedClaim(CitizenClaimIssuedEvent event) {
        Document sealedClaimDocument = citizenServiceDocumentsService.sealedClaimDocument(event.getClaim());

        PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(event.getClaim().getReferenceNumber()),
            createPdf(sealedClaimDocument));

        Document defendantLetterDocument = citizenServiceDocumentsService.pinLetterDocument(
            event.getClaim(),
            event.getPin()
        );

        PDF defendantLetter = new PDF(
            buildDefendantLetterFileBaseName(event.getClaim().getReferenceNumber()),
            createPdf(defendantLetterDocument)
        );

        publisher.publishEvent(new DocumentReadyToPrintEvent(defendantLetterDocument, sealedClaimDocument));
        publisher.publishEvent(new DocumentGeneratedEvent(event.getClaim(), event.getAuthorisation(),
            sealedClaim, defendantLetter));
    }

    @EventListener
    public void generateForRepresentedClaim(RepresentedClaimIssuedEvent event) {
        PDF sealedClaim = new PDF(buildSealedClaimFileBaseName(event.getClaim().getReferenceNumber()),
            legalSealedClaimPdfService.createPdf(event.getClaim()));

        publisher.publishEvent(new DocumentGeneratedEvent(event.getClaim(), event.getAuthorisation(), sealedClaim));
    }

    private byte[] createPdf(Document document) {
        requireNonNull(document);

        return pdfServiceClient.generateFromHtml(document.template.getBytes(), document.values);
    }
}
