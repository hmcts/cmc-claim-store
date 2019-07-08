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
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(MockitoJUnitRunner.class)
public class UploadOperationServiceTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final String AUTHORISATION = "AUTHORISATION";
    private static final PDF SEALED_CLAIM_PDF = new PDF("000MC001-claim", "test".getBytes(), SEALED_CLAIM);

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

        uploadOperationService.uploadDocument(CLAIM, AUTHORISATION, SEALED_CLAIM_PDF);
        //verify

        verify(documentsService).uploadToDocumentManagement(eq(SEALED_CLAIM_PDF), eq(AUTHORISATION), eq(CLAIM));
    }

    @Test(expected = DocumentManagementException.class)
    public void shouldNotUpdateFlagWhenUploadDocumentFails() {
        //when
        when(documentsService.uploadToDocumentManagement(eq(SEALED_CLAIM_PDF), eq(AUTHORISATION), eq(CLAIM)))
            .thenThrow(new DocumentManagementException("bad files"));

        try {
            uploadOperationService.uploadDocument(CLAIM, AUTHORISATION, SEALED_CLAIM_PDF);
        } finally {

            //verify
            verify(documentsService).uploadToDocumentManagement(eq(SEALED_CLAIM_PDF), eq(AUTHORISATION), eq(CLAIM));
        }
    }
}
