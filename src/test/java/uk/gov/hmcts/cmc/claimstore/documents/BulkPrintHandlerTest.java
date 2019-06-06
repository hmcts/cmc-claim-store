package uk.gov.hmcts.cmc.claimstore.documents;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintHandlerTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @Test
    public void notifyStaff() {
        //given
        BulkPrintHandler bulkPrintHandler = new BulkPrintHandler(bulkPrintService);
        Claim claim = SampleClaim.getDefault();
        Map<String, Object> pinContents = new HashMap<>();
        Document defendantLetterDocument = new Document("pinTemplate", pinContents);
        Map<String, Object> claimContents = new HashMap<>();
        Document sealedClaimDocument = new Document("sealedClaimTemplate", claimContents);

        DocumentReadyToPrintEvent printEvent
            = new DocumentReadyToPrintEvent(claim, defendantLetterDocument, sealedClaimDocument);

        //when
        bulkPrintHandler.print(printEvent);

        //verify
        verify(bulkPrintService).print(
            claim,
            ImmutableMap.of(
                ClaimDocumentType.DEFENDANT_PIN_LETTER, defendantLetterDocument,
                ClaimDocumentType.SEALED_CLAIM, sealedClaimDocument
            ));
    }
}
