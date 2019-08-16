package uk.gov.hmcts.cmc.claimstore.documents.content.directionsquestionnaire;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimContentProvider;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantDirectionsQuestionnaireContentProviderTest {

    @Mock
    private ClaimContentProvider claimContentProvider;

    private ClaimantDirectionsQuestionnaireContentProvider contentProvider;

    @Before
    public void setUp() {
        contentProvider = new ClaimantDirectionsQuestionnaireContentProvider(claimContentProvider,
            new HearingContentProvider());
        Mockito.when(claimContentProvider.createContent(any())).thenReturn(Collections.emptyMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenNoResponse() {
        contentProvider.createContent(SampleClaim.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenResponseIsAcceptation() {
        contentProvider.createContent(SampleClaim.getWithClaimantResponse());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionWhenQuestionnaireIsNull() {
        contentProvider.createContent(
            SampleClaim.getWithClaimantResponse(SampleClaimantResponse.validDefaultRejection()));
    }

    @Test
    public void providesResponseTime() {
        Map<String, Object> result = contentProvider.createContent(
            SampleClaim.getWithClaimantResponse(SampleClaimantResponse.validRejectionWithDirectionsQuestionnaire()));

        assertTrue(result.containsKey("hearingContent"));
        assertTrue(result.containsKey("claimantSubmittedOn"));

    }
}
