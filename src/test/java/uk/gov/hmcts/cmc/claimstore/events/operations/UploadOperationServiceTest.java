package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

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
    private DocumentsService documentsService;
    private UploadOperationService uploadOperationService;

    @Before
    public void before() {
        uploadOperationService = new UploadOperationService(documentsService);
    }

    @Test
    public void shouldUploadDocument() {
        //when
        when(documentsService.uploadToDocumentManagement(any(), eq(AUTHORISATION), eq(CLAIM))).thenReturn(CLAIM);

        uploadOperationService.uploadDocument(CLAIM, AUTHORISATION, pinLetterClaim);
        //verify

        verify(documentsService).uploadToDocumentManagement(eq(pinLetterClaim), eq(AUTHORISATION), eq(CLAIM));
    }

    @Test(expected = DocumentManagementException.class)
    public void shouldNotUpdateFlagWhenUploadDocumentFails() {
        //when
        when(documentsService.uploadToDocumentManagement(eq(pinLetterClaim), eq(AUTHORISATION), eq(CLAIM)))
            .thenThrow(new DocumentManagementException("bad files"));

        try {
            uploadOperationService.uploadDocument(CLAIM, AUTHORISATION, pinLetterClaim);
        } finally {

            //verify
            verify(documentsService).uploadToDocumentManagement(eq(pinLetterClaim), eq(AUTHORISATION), eq(CLAIM));
        }
    }
}
