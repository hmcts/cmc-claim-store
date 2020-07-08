package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.DefendantPinLetterContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CitizenServiceDocumentsServiceTest {
    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};

    private CitizenServiceDocumentsService citizenServiceDocumentsService;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private ClaimContentProvider claimContentProvider;
    @Mock
    private DefendantPinLetterContentProvider letterContentProvider;

    @Before
    public void beforeEachTest() {
        citizenServiceDocumentsService
            = new CitizenServiceDocumentsService(documentTemplates, claimContentProvider, letterContentProvider);
    }

    @Test
    public void shouldReturnSealedClaimDocument() {
        //given
        when(documentTemplates.getSealedClaim()).thenReturn(PDF_BYTES);

        Claim claim = SampleClaim.getDefault();
        Map<String, Object> documentContent = new HashMap<>();
        documentContent.put("referenceNumber", "000MC001");
        when(claimContentProvider.createContent(claim)).thenReturn(documentContent);
        //when
        Document result = citizenServiceDocumentsService.sealedClaimDocument(claim);
        //then
        assertThat(result.template).isEqualTo(new String(PDF_BYTES));

        //verify
        verify(claimContentProvider).createContent(eq(claim));
        verify(documentTemplates).getSealedClaim();
    }

    @Test
    public void shouldReturnPinLetterDocument() {
        //given
        when(documentTemplates.getDefendantPinLetter()).thenReturn(PDF_BYTES);

        Claim claim = SampleClaim.getDefault();
        Map<String, Object> documentContent = new HashMap<>();
        documentContent.put("referenceNumber", "000MC001");
        String defendantPin = "67Khy890";
        when(letterContentProvider.createContent(claim, defendantPin)).thenReturn(documentContent);
        //when
        Document result = citizenServiceDocumentsService.pinLetterDocument(claim, defendantPin);
        //then
        assertThat(result.template).isEqualTo(new String(PDF_BYTES));

        //verify
        verify(letterContentProvider).createContent(eq(claim), eq(defendantPin));
        verify(documentTemplates).getDefendantPinLetter();
    }
}
