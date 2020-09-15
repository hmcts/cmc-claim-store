package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.documents.CitizenServiceDocumentsService;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentGeneratedEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildDefendantLetterFileBaseName;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;

@Component
public class DocumentGenerator {

    private final CitizenServiceDocumentsService citizenServiceDocumentsService;
    private final SealedClaimPdfService sealedClaimPdfService;
    private final ApplicationEventPublisher publisher;
    private final PDFServiceClient pdfServiceClient;
    private final PrintableDocumentService printableDocumentService;
    private final LaunchDarklyClient launchDarklyClient;

    @Autowired
    public DocumentGenerator(
        CitizenServiceDocumentsService citizenServiceDocumentsService,
        SealedClaimPdfService sealedClaimPdfService,
        ApplicationEventPublisher publisher,
        PDFServiceClient pdfServiceClient,
        PrintableDocumentService printableDocumentService,
        LaunchDarklyClient launchDarklyClient
    ) {
        this.citizenServiceDocumentsService = citizenServiceDocumentsService;
        this.sealedClaimPdfService = sealedClaimPdfService;
        this.publisher = publisher;
        this.pdfServiceClient = pdfServiceClient;
        this.printableDocumentService = printableDocumentService;
        this.launchDarklyClient = launchDarklyClient;
    }

    @EventListener
    @LogExecutionTime
    public void generateForNonRepresentedClaim(CitizenClaimIssuedEvent event) {
        Document sealedClaimDoc = citizenServiceDocumentsService.sealedClaimDocument(event.getClaim());

        Document defendantLetterDoc;
        CCDDocument pinLetter = null;
        PDF defendantLetter;
        if (launchDarklyClient.isFeatureEnabled("new-defendant-pin-letter", LaunchDarklyClient.CLAIM_STORE_USER)) {
            pinLetter = citizenServiceDocumentsService.createDefendantPinLetter(event.getClaim(),
                event.getPin(),
                event.getAuthorisation());
            defendantLetterDoc = printableDocumentService.process(pinLetter, event.getAuthorisation());
        } else {
            defendantLetterDoc = citizenServiceDocumentsService.pinLetterDocument(event.getClaim(),
                event.getPin());
        }

        publisher.publishEvent(
            new DocumentReadyToPrintEvent(
                event.getClaim(),
                defendantLetterDoc,
                sealedClaimDoc,
                event.getAuthorisation()));

        PDF sealedClaim = sealedClaimPdfService.createPdf(event.getClaim());
        if (launchDarklyClient.isFeatureEnabled("new-defendant-pin-letter", LaunchDarklyClient.CLAIM_STORE_USER)) {
            defendantLetter = new PDF(buildDefendantLetterFileBaseName(event.getClaim().getReferenceNumber()),
                printableDocumentService.pdf(pinLetter, event.getAuthorisation()), DEFENDANT_PIN_LETTER);
        } else {
            defendantLetter = new PDF(buildDefendantLetterFileBaseName(event.getClaim().getReferenceNumber()),
                pdfServiceClient.generateFromHtml(defendantLetterDoc.template.getBytes(), defendantLetterDoc.values),
                DEFENDANT_PIN_LETTER);
        }
        publisher.publishEvent(new DocumentGeneratedEvent(event.getClaim(), event.getAuthorisation(),
            sealedClaim, defendantLetter));
    }

    @EventListener
    @LogExecutionTime
    public void generateForRepresentedClaim(RepresentedClaimIssuedEvent event) {
        PDF sealedClaim = sealedClaimPdfService.createPdf(event.getClaim());
        publisher.publishEvent(new DocumentGeneratedEvent(event.getClaim(), event.getAuthorisation(), sealedClaim));
    }

    public void generateForCitizenRPA(CitizenClaimIssuedEvent event) {
        PDF sealedClaim = sealedClaimPdfService.createPdf(event.getClaim());
        publisher.publishEvent(new DocumentGeneratedEvent(event.getClaim(), event.getAuthorisation(),
            sealedClaim));
    }
}
