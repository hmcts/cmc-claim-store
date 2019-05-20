package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;

@RunWith(MockitoJUnitRunner.class)
public class UploadOperationServiceTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final String AUTHORISATION = "AUTHORISATION";
    private static final PDF pinLetterClaim = new PDF("0000-pin", "test".getBytes(), DEFENDANT_PIN_LETTER);

    @Mock
    private DocumentsService documentsService;
    @Mock
    private ClaimCreationEventsStatusService eventsStatusService;

    @Test
    public void shouldUploadDocument() {
        //Given
        UploadOperationService uploadOperationService = new UploadOperationService(documentsService,
            eventsStatusService);
        //when
        when(documentsService.uploadToDocumentManagement(any(), eq(AUTHORISATION), eq(CLAIM))).thenReturn(CLAIM);

        uploadOperationService.uploadDocument(CLAIM, AUTHORISATION, pinLetterClaim, CLAIM_ISSUE_RECEIPT_UPLOAD);
        //verify

        verify(documentsService).uploadToDocumentManagement(eq(pinLetterClaim), eq(AUTHORISATION), eq(CLAIM));
        verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM),
            eq(CLAIM_ISSUE_RECEIPT_UPLOAD));
    }

    @Test(expected = DocumentManagementException.class)
    public void shouldNotUpdateFlagWhenUploadDocumentFails() throws Exception {
        //Given
        UploadOperationService uploadOperationService = new UploadOperationService(documentsService,
            eventsStatusService);
        //when
        when(documentsService.uploadToDocumentManagement(eq(pinLetterClaim), eq(AUTHORISATION), eq(CLAIM)))
            .thenThrow(new DocumentManagementException("bad files"));

        try {
            uploadOperationService.uploadDocument(CLAIM, AUTHORISATION, pinLetterClaim, CLAIM_ISSUE_RECEIPT_UPLOAD);
        } finally {

            //verify
            verify(documentsService).uploadToDocumentManagement(eq(pinLetterClaim), eq(AUTHORISATION), eq(CLAIM));
            verify(eventsStatusService, never()).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM),
                eq(CLAIM_ISSUE_RECEIPT_UPLOAD));
        }
    }
}
