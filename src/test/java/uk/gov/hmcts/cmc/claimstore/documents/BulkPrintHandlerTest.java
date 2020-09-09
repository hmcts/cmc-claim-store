package uk.gov.hmcts.cmc.claimstore.documents;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.Printable;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintablePdf;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.claimstore.events.BulkPrintTransferEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType.BULK_PRINT_TRANSFER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType.DIRECTION_ORDER_LETTER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType.FIRST_CONTACT_LETTER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType.GENERAL_LETTER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildOconFormFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildPaperDefenceCoverLetterFileBaseName;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintHandlerTest {

    private static final String AUTHORISATION = "Bearer: let me in";
    @Mock
    private BulkPrintService bulkPrintService;

    @Test
    public void notifyStaffForDefendantLetters() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService);
        Claim claim = SampleClaim.getDefault();
        Document defendantLetterDocument = new Document("pinTemplate", new HashMap<>());
        Document sealedClaimDocument = new Document("sealedClaimTemplate", new HashMap<>());

        DocumentReadyToPrintEvent printEvent
            = new DocumentReadyToPrintEvent(claim, defendantLetterDocument, sealedClaimDocument, AUTHORISATION);

        //when
        bulkPrintHandler.print(printEvent);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.of(
                new PrintableTemplate(
                    defendantLetterDocument,
                    claim.getReferenceNumber() + "-defendant-pin-letter"),
                new PrintableTemplate(
                    sealedClaimDocument,
                    claim.getReferenceNumber() + "-claim-form")
            ),
            FIRST_CONTACT_LETTER_TYPE,
            AUTHORISATION
        );
    }

    @Test
    public void notifyStaffForLegalAdvisor() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService);
        Claim claim = SampleClaim.getDefault();
        Document coverSheet = new Document("coverSheet", new HashMap<>());
        Document legalOrder = new Document("legalOrder", new HashMap<>());

        //when
        bulkPrintHandler.printDirectionOrder(claim, coverSheet, legalOrder, AUTHORISATION);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.of(
                new PrintableTemplate(
                    coverSheet,
                    claim.getReferenceNumber() + "-directions-order-cover-sheet"),
                new PrintablePdf(
                    legalOrder,
                    claim.getReferenceNumber() + "-directions-order")
            ),
            DIRECTION_ORDER_LETTER_TYPE,
            AUTHORISATION);
    }

    @Test
    public void notifyForGeneralLetter() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService);
        Claim claim = SampleClaim.getDefault();
        Document generalLetter = new Document("letter", new HashMap<>());

        //when
        bulkPrintHandler.printGeneralLetter(claim, generalLetter, AUTHORISATION);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.of(
                new PrintablePdf(
                    generalLetter,
                    claim.getReferenceNumber() + "-general-letter-"
                        + LocalDate.now())
            ),
            GENERAL_LETTER_TYPE,
            AUTHORISATION
        );
    }

    @Test
    public void notifyForBulkPrintTransferEvent() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService);
        Claim claim = mock(Claim.class);
        when(claim.getReferenceNumber()).thenReturn("AAA");

        Document coverLetter = new Document("letter", new HashMap<>());
        Document caseDocument = mock(Document.class);
        String caseDocumentFileName = "caseDoc.pdf";

        List<BulkPrintTransferEvent.PrintableDocument> caseDocuments = List.of(
            new BulkPrintTransferEvent.PrintableDocument(caseDocument, caseDocumentFileName)
        );

        //when
        bulkPrintHandler.printBulkTransferDocs(claim, coverLetter, caseDocuments, AUTHORISATION);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            List.of(
                new PrintablePdf(
                    coverLetter,
                    claim.getReferenceNumber() + "-directions-order-cover-sheet"),
                new PrintablePdf(
                    caseDocument,
                    caseDocumentFileName
                )
            ),
            BULK_PRINT_TRANSFER_TYPE,
            AUTHORISATION);
    }

    @Test
    public void notifyPaperDefenceLetter() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService);
        Claim claim = SampleClaim.getDefault();
        Document letter = new Document("letter", new HashMap<>());

        //when
        bulkPrintHandler.printPaperDefence(claim, letter, letter, AUTHORISATION);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.<Printable>builder()
                .add(new PrintablePdf(
                    letter,
                    buildPaperDefenceCoverLetterFileBaseName(claim.getReferenceNumber())))
                .add(new PrintablePdf(
                    letter,
                    buildOconFormFileBaseName(claim.getReferenceNumber())))
                .build(),
            GENERAL_LETTER_TYPE,
            AUTHORISATION
        );
    }
}
