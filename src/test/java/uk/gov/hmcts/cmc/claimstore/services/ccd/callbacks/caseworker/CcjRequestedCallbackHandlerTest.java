package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CcjRequestedCallbackHandlerTest {

    private CcjRequestedCallbackHandler handler;
    private CallbackParams callbackParams;
    private CallbackRequest callbackRequest;
    private static final String AUTHORISATION = "Bearer: aaaa";

    @BeforeEach
    public void setUp() {
        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.EMPTY_MAP).build())
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
        handler = new CcjRequestedCallbackHandler(false);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

        Assertions.assertEquals(ClaimState.OPEN.getValue(), response.getData().get("state"));
    }

    @Test
    void shouldReturnJudgmentRequestedStateIfCtscEnabled() {
        handler = new CcjRequestedCallbackHandler(true);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

        Assertions.assertEquals(ClaimState.JUDGMENT_REQUESTED.getValue(), response.getData().get("state"));
    }
}
