package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.DocumentUploadHandler;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;

@RunWith(MockitoJUnitRunner.class)
public class UploadOperationServiceTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final String AUTHORISATION = "AUTHORISATION";
    public static final PDF pinLetterClaim = new PDF("0000-pin", "test".getBytes(), DEFENDANT_PIN_LETTER);

    @Mock
    private DocumentUploadHandler documentUploadHandler;

    @Test
    public void shouldUploadDocument() {
        //when
        UploadOperationService uploadOperationService = new UploadOperationService(documentUploadHandler);

        uploadOperationService.uploadDocument(CLAIM, AUTHORISATION, pinLetterClaim);
        //verify

        verify(documentUploadHandler)
            .uploadToDocumentManagement(eq(CLAIM), eq(AUTHORISATION), eq(singletonList(pinLetterClaim)));
    }
}
