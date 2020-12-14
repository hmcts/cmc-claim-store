package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Paper Response Reviewed handler")
class PaperResponseReviewedCallbackHandlerTest {

    @InjectMocks
    private PaperResponseReviewedCallbackHandler paperResponseReviewCallbackHandler;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    LaunchDarklyClient launchDarklyClient;

    private CallbackRequest callbackRequest;

    private Claim claim;

    private static final String ALREADY_RESPONDED_ERROR = "You can’t process this paper request "
        + "because the defendant already responded to the claim";

    @Captor
    private ArgumentCaptor<Claim> claimArgumentCaptor;

    @BeforeEach
    void setUp() {
        callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.MORE_TIME_REQUESTED_PAPER.getValue())
            .caseDetails(CaseDetails.builder()
                .id(10L)
                .data(Collections.emptyMap())
                .build()).build();
    }

    @Test
    @DisplayName("should include error when already responded online if LD enabled for restrict-review-paper-response")
    void eventNotPossibleWhenRespondedOnline() {
        when(launchDarklyClient.isFeatureEnabled("restrict-review-paper-response")).thenReturn(true);
        claim = SampleClaim.getWithDefaultResponse();
        checkEventAllowed(claim, true);
    }

    @Test
    @DisplayName("should include error when already responded offline if LD enabled for restrict-review-paper-response")
    void eventNotPossibleWhenRespondedOffline() {
        when(launchDarklyClient.isFeatureEnabled("restrict-review-paper-response")).thenReturn(true);
        claim = SampleClaim.withFullClaimData().toBuilder().respondedAt(LocalDateTime.now()).build();
        checkEventAllowed(claim, true);
    }

    @Test
    @DisplayName("should include error when already responded online if LD enabled for restrict-review-paper-response")
    void eventPossibleWhenRespondedOnlineIfNotRestricted() {
        claim = SampleClaim.getWithDefaultResponse();
        checkEventAllowed(claim, false);
    }

    @Test
    @DisplayName("should include error when already responded offline if LD enabled for restrict-review-paper-response")
    void eventPossibleWhenRespondedOfflineIfNotRestricted() {
        claim = SampleClaim.withFullClaimData().toBuilder().respondedAt(LocalDateTime.now()).build();
        checkEventAllowed(claim, false);
    }

    private void checkEventAllowed(Claim claim, boolean errorExpected) {

        when(caseDetailsConverter.extractClaim(any())).thenReturn(this.claim);

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .build();

        var response = (AboutToStartOrSubmitCallbackResponse) paperResponseReviewCallbackHandler.handle(callbackParams);
        assertEquals(errorExpected, response.getErrors() != null
            && response.getErrors().contains(ALREADY_RESPONDED_ERROR));
    }

}
