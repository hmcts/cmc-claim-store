package uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimContentProvider;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ClaimantDirectionsQuestionnaireContentProviderTest {

    @Mock
    private ClaimContentProvider claimContentProvider;

    private ClaimantDirectionsQuestionnaireContentProvider contentProvider;

    @BeforeEach
    public void setUp() {
        contentProvider = new ClaimantDirectionsQuestionnaireContentProvider(claimContentProvider,
            new HearingContentProvider());
        Mockito.lenient().when(claimContentProvider.createContent(any())).thenReturn(Collections.emptyMap());
    }

    @Test
    public void throwsExceptionWhenNoResponse() {
        assertThrows(IllegalArgumentException.class, () -> {
            contentProvider.createContent(SampleClaim.getDefault());
        });
    }

    @Test
    public void throwsExceptionWhenResponseIsAcceptation() {
        assertThrows(IllegalArgumentException.class, () -> {
            contentProvider.createContent(SampleClaim.getWithClaimantResponse());
        });
    }

    @Test
    public void throwsExceptionWhenQuestionnaireIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            contentProvider.createContent(
                SampleClaim.getWithClaimantResponse(SampleClaimantResponse.validDefaultRejection()));
        });
    }

    @Test
    public void providesResponseTime() {
        Map<String, Object> result = contentProvider.createContent(
            SampleClaim.getWithClaimantResponse(SampleClaimantResponse.validRejectionWithDirectionsQuestionnaire()));

        assertTrue(result.containsKey("hearingContent"));
        assertTrue(result.containsKey("claimantSubmittedOn"));

    }
}
