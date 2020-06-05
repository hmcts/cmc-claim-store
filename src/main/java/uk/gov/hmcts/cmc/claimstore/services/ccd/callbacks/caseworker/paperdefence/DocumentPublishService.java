package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

@Service
public class DocumentPublishService {
    private final IssuePaperResponseLetterService issuePaperResponseLetterService;
    private final PrintableDocumentService printableDocumentService;
    private final EventProducer eventProducer;

    @Autowired
    public DocumentPublishService(
        IssuePaperResponseLetterService issuePaperResponseLetterService,
        PrintableDocumentService printableDocumentService,
        EventProducer eventProducer
    ) {
        this.issuePaperResponseLetterService = issuePaperResponseLetterService;
        this.printableDocumentService = printableDocumentService;
        this.eventProducer = eventProducer;
    }

    public CCDCase publishDocuments(CCDCase ccdCase, Claim claim, String authorisation) {
        CCDDocument coverLetter = issuePaperResponseLetterService.createCoverLetter(ccdCase, authorisation);
        Document coverDoc = printableDocumentService.process(coverLetter, authorisation);
        CCDDocument oconForm = issuePaperResponseLetterService.createOconForm(ccdCase, claim, authorisation);
        Document formDoc = printableDocumentService.process(oconForm, authorisation);

        eventProducer.createPaperDefenceEvent(claim, coverDoc, formDoc);
        return issuePaperResponseLetterService.getUpdatedCaseWithDocuments(ccdCase, claim, coverLetter);
    }
}
