package uk.gov.hmcts.cmc.claimstore.documents;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintablePdf;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.claimstore.events.BulkPrintTransferEvent;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.GeneralLetterReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.legaladvisor.DirectionsOrderReadyToPrintEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.BULK_PRINT_TRANSFER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.DIRECTION_ORDER_LETTER_TYPE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.GENERAL_LETTER_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintHandlerTest {

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
            = new DocumentReadyToPrintEvent(claim, defendantLetterDocument, sealedClaimDocument);

        //when
        bulkPrintHandler.print(printEvent);

        //verify
        verify(bulkPrintService).print(
            claim,
            ImmutableList.of(
                new PrintableTemplate(
                    defendantLetterDocument,
                    claim.getReferenceNumber() + "-defendant-pin-letter"),
                new PrintableTemplate(
                    sealedClaimDocument,
                    claim.getReferenceNumber() + "-claim-form")
            ));
    }

    @Test
    public void notifyStaffForLegalAdvisor() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService);
        Claim claim = SampleClaim.getDefault();
        Document coverSheet = new Document("coverSheet", new HashMap<>());
        Document legalOrder = new Document("legalOrder", new HashMap<>());

        DirectionsOrderReadyToPrintEvent printEvent
            = new DirectionsOrderReadyToPrintEvent(claim, coverSheet, legalOrder);

        //when
        bulkPrintHandler.print(printEvent);

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
            ), DIRECTION_ORDER_LETTER_TYPE);
    }

    @Test
    public void notifyForGeneralLetter() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService);
        Claim claim = SampleClaim.getDefault();
        Document generalLetter = new Document("letter", new HashMap<>());

        GeneralLetterReadyToPrintEvent printEvent
            = new GeneralLetterReadyToPrintEvent(claim, generalLetter);

        //when
        bulkPrintHandler.print(printEvent);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.of(
                new PrintablePdf(
                    generalLetter,
                    claim.getReferenceNumber() + "-general-letter-"
                        + LocalDate.now())
            ), GENERAL_LETTER_TYPE);
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

        BulkPrintTransferEvent printEvent = new BulkPrintTransferEvent(claim, coverLetter, caseDocuments);

        //when
        bulkPrintHandler.print(printEvent);

        //verify
        verify(bulkPrintService).printPdf(
            claim,
            ImmutableList.of(
                new PrintablePdf(
                    coverLetter,
                    claim.getReferenceNumber() + "-directions-order-cover-sheet"),
                new PrintablePdf(
                    caseDocument,
                    caseDocumentFileName
                )
            ), BULK_PRINT_TRANSFER_TYPE);
    }
}
