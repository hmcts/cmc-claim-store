package uk.gov.hmcts.cmc.claimstore.documents;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintablePdf;
import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.PrintableTemplate;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.events.legaladvisor.DirectionsOrderReadyToPrintEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;

import static org.mockito.Mockito.verify;

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
            ));
    }
}
