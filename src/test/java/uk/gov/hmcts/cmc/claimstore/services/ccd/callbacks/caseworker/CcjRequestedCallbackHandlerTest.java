package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CcjRequestedCallbackHandlerTest {

    private CcjRequestedCallbackHandler handler;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseMapper caseMapper;

    private Claim sampleLinkedClaim = SampleClaim.builder()
        .withState(ClaimState.OPEN).build();
    private CallbackRequest callbackRequest;
    private CallbackParams callbackParams;
    private static final String AUTHORISATION = "Bearer: aaaa";

    @BeforeEach
    public void setUp() {
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
            .thenReturn(CCDCase.builder().state("open").build());
        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Maps.newHashMap()).build())
            .eventId(CaseEvent.CCJ_REQUESTED.getValue())
            .build();
        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
    }

    @Test
    void shouldReturnOpenStateIfCtscNotEnabled() {
        when(caseDetailsConverter.convertToMap(any(CCDCase.class)))
            .thenReturn(ImmutableMap.of("state", "open"));
        handler = new CcjRequestedCallbackHandler(
            false,
            caseDetailsConverter,
            caseMapper);
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        Assertions.assertEquals(ClaimState.OPEN.getValue(), response.getData().get("state"));
    }

    @Test
    void shouldReturnJudgmentRequestedStateIfCtscEnabled() {
        when(caseDetailsConverter.convertToMap(any(CCDCase.class)))
            .thenReturn(ImmutableMap.of("state", "judgmentRequested"));
        handler = new CcjRequestedCallbackHandler(
            true,
            caseDetailsConverter,
            caseMapper);
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        Assertions.assertEquals(ClaimState.JUDGMENT_REQUESTED.getValue(), response.getData().get("state"));
    }
}
