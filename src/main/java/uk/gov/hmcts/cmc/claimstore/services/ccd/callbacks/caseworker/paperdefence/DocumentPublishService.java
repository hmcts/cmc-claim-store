package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.time.LocalDate;

@Service
public class DocumentPublishService {
    private final PaperResponseLetterService paperResponseLetterService;
    private final PrintableDocumentService printableDocumentService;
    private final EventProducer eventProducer;

    @Autowired
    public DocumentPublishService(
        PaperResponseLetterService paperResponseLetterService,
        PrintableDocumentService printableDocumentService,
        EventProducer eventProducer
    ) {
        this.paperResponseLetterService = paperResponseLetterService;
        this.printableDocumentService = printableDocumentService;
        this.eventProducer = eventProducer;
    }

    public CCDCase publishDocuments(
        CCDCase ccdCase,
        Claim claim,
        String authorisation,
        LocalDate extendedResponseDeadline
    ) {
        CCDDocument coverLetter = paperResponseLetterService
            .createCoverLetter(ccdCase, authorisation, extendedResponseDeadline);

        Document coverDoc = printableDocumentService.process(coverLetter, authorisation);

//        CCDDocument oconForm = issuePaperResponseLetterService
//        .createOconForm(ccdCase, claim, authorisation, extendedResponseDeadline);

        Document formDoc = null;
//            = printableDocumentService.process(oconForm, authorisation);

        eventProducer.createPaperDefenceEvent(claim, coverDoc, formDoc);
        return paperResponseLetterService
            .addCoverLetterToCaseWithDocuments(ccdCase, claim, coverLetter, authorisation);
    }
}
