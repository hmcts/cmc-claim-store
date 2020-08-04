package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.time.LocalDate;

@Service
public class DocumentPublishService {
    private final PaperResponseLetterService paperResponseLetterService;
    private final PrintableDocumentService printableDocumentService;
    private final BulkPrintHandler bulkPrintHandler;

    @Autowired
    public DocumentPublishService(
        PaperResponseLetterService paperResponseLetterService,
        PrintableDocumentService printableDocumentService,
        BulkPrintHandler bulkPrintHandler
    ) {
        this.paperResponseLetterService = paperResponseLetterService;
        this.printableDocumentService = printableDocumentService;
        this.bulkPrintHandler = bulkPrintHandler;
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

        CCDDocument oconForm = paperResponseLetterService
            .createOconForm(ccdCase, claim, authorisation, extendedResponseDeadline);

        Document formDoc = printableDocumentService.process(oconForm, authorisation);

        bulkPrintHandler.printPaperDefence(claim, coverDoc, formDoc, authorisation);

        return paperResponseLetterService
            .addCoverLetterToCaseWithDocuments(ccdCase, claim, coverLetter, authorisation);
    }
}
