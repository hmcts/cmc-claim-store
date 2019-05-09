package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentUploadHandler;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;

@RunWith(MockitoJUnitRunner.class)
public class UploadOperationServiceTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final String AUTHORISATION = "AUTHORISATION";
    private static final PDF pinLetterClaim = new PDF("0000-pin", "test".getBytes(), DEFENDANT_PIN_LETTER);

    @Mock
    private DocumentUploadHandler documentUploadHandler;
    @Mock
    private ClaimCreationEventsStatusService eventsStatusService;

    @Test
    public void shouldUploadDocument() {
        //Given
        UploadOperationService uploadOperationService = new UploadOperationService(documentUploadHandler,
            eventsStatusService);
        //when
        when(documentUploadHandler.uploadToDocumentManagement(eq(CLAIM), eq(AUTHORISATION), any())).thenReturn(CLAIM);

        uploadOperationService.uploadDocument(CLAIM, AUTHORISATION, pinLetterClaim,
            CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD);
        //verify

        verify(documentUploadHandler)
            .uploadToDocumentManagement(eq(CLAIM), eq(AUTHORISATION), eq(singletonList(pinLetterClaim)));
        verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM),
            eq(CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD));
    }
}
