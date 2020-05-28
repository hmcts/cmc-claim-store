package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.events.BulkPrintTransferEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransferCaseLetterSender {

    private final GeneralLetterService generalLetterService;
    private final PrintableDocumentService printableDocumentService;
    private final EventProducer eventProducer;

    public TransferCaseLetterSender(GeneralLetterService generalLetterService,
                                    PrintableDocumentService printableDocumentService,
                                    EventProducer eventProducer) {
        this.generalLetterService = generalLetterService;
        this.printableDocumentService = printableDocumentService;
        this.eventProducer = eventProducer;
    }

    public void sendNoticeOfTransferForDefendant(String authorisation, CCDCase ccdCase, Claim claim) {

        generalLetterService.printLetter(authorisation, ccdCase.getDraftLetterDoc(), claim);
    }

    public void sendAllCaseDocumentsToCourt(String authorisation, CCDCase ccdCase, Claim claim) {

        Document coverLetterDoc = printableDocumentService.process(ccdCase.getCoverLetterDoc(), authorisation);
        List<BulkPrintTransferEvent.PrintableDocument> caseDocuments = getDocumentsExcludingCoverNote(ccdCase, authorisation);
        eventProducer.createBulkPrintTransferEvent(claim, coverLetterDoc, caseDocuments);
    }

    private List<BulkPrintTransferEvent.PrintableDocument> getDocumentsExcludingCoverNote(CCDCase ccdCase, String authorisation) {
        return ccdCase.getCaseDocuments().stream()
            .map(CCDCollectionElement::getValue)
            .map(CCDClaimDocument::getDocumentLink)
            .filter(d -> !d.getDocumentUrl().equals(ccdCase.getCoverLetterDoc().getDocumentUrl()))
            .map(d -> this.getPrintableDocument(authorisation, d))
            .collect(Collectors.toList());
    }

    private BulkPrintTransferEvent.PrintableDocument getPrintableDocument(String authorisation, CCDDocument document) {
        Document printableDocument = printableDocumentService.process(document, authorisation);
        return new BulkPrintTransferEvent.PrintableDocument(printableDocument, document.getDocumentFileName());
    }
}
