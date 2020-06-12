package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
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

    public void sendNoticeOfTransferForDefendant(String authorisation, CCDDocument ccdDocument, Claim claim) {

        generalLetterService.printLetter(authorisation, ccdDocument, claim);
    }

    public void sendAllCaseDocumentsToCourt(String authorisation, CCDCase ccdCase, Claim claim, CCDDocument coverDoc) {

        Document coverLetterDoc = printableDocumentService.process(coverDoc, authorisation);
        List<BulkPrintTransferEvent.PrintableDocument> caseDocuments = getAllCaseDocuments(ccdCase, authorisation);
        eventProducer.createBulkPrintTransferEvent(claim, coverLetterDoc, caseDocuments,authorisation);
    }

    private List<BulkPrintTransferEvent.PrintableDocument> getAllCaseDocuments(CCDCase ccdCase, String authorisation) {

        var printableDocuments = ImmutableList.<BulkPrintTransferEvent.PrintableDocument>builder()
            .addAll(getClaimDocuments(ccdCase, authorisation));

        List<BulkPrintTransferEvent.PrintableDocument> scannedDocuments = scannedDocuments(ccdCase, authorisation);

        if (scannedDocuments != null && !scannedDocuments.isEmpty()) {
            printableDocuments.addAll(scannedDocuments);
        }

        return printableDocuments.build();
    }

    private List<BulkPrintTransferEvent.PrintableDocument> getClaimDocuments(CCDCase ccdCase, String authorisation) {
        var claimsDocuments = ImmutableList.<CCDCollectionElement<CCDClaimDocument>>builder()
            .addAll(ccdCase.getCaseDocuments());

        List<CCDCollectionElement<CCDClaimDocument>> uploadedDocuments = ccdCase.getStaffUploadedDocuments();

        if (uploadedDocuments != null && !uploadedDocuments.isEmpty()) {
            claimsDocuments.addAll(uploadedDocuments);
        }

        return claimsDocuments.build().stream()
            .map(CCDCollectionElement::getValue)
            .filter(d -> !d.getDocumentType().equals(CCDClaimDocumentType.CLAIM_ISSUE_RECEIPT))
            .map(CCDClaimDocument::getDocumentLink)
            .map(d -> this.getPrintableDocument(authorisation, d))
            .collect(Collectors.toList());
    }

    private List<BulkPrintTransferEvent.PrintableDocument> scannedDocuments(CCDCase ccdCase, String authorisation) {
        if (ccdCase.getScannedDocuments() == null) {
            return List.of();
        }

        return ccdCase.getScannedDocuments().stream()
            .map(CCDCollectionElement::getValue)
            .map(CCDScannedDocument::getUrl)
            .map(d -> this.getPrintableDocument(authorisation, d))
            .collect(Collectors.toList());
    }

    private BulkPrintTransferEvent.PrintableDocument getPrintableDocument(String authorisation, CCDDocument document) {
        Document printableDocument = printableDocumentService.process(document, authorisation);
        return new BulkPrintTransferEvent.PrintableDocument(printableDocument, document.getDocumentFileName());
    }
}
