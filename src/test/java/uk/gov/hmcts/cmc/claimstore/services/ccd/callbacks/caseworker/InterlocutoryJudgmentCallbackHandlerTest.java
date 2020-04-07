package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INTERLOCUTORY_JUDGMENT;

@ExtendWith(MockitoExtension.class)
class InterlocutoryJudgmentCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private InterlocutoryJudgmentCallbackHandler handler;

    private CallbackParams params;

    private CCDCase ccdCase;

    @BeforeEach
    void setUp() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(INTERLOCUTORY_JUDGMENT.getValue())
            .build();

        params = CallbackParams.builder()
            .request(callbackRequest)
            .type(CallbackType.ABOUT_TO_SUBMIT).build();

        ccdCase = CCDCase.builder()
            .build();
        when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);
    }

    @Test
    void shouldReturnOpenStateIfCtscDisabled() {

        handler = new InterlocutoryJudgmentCallbackHandler(caseDetailsConverter, false);

        String state = ClaimState.OPEN.getValue();
        when(caseDetailsConverter.convertToMap(ccdCase)).thenReturn(ImmutableMap.of("state", state));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CCDCase updatedCcdCase = CCDCase.builder()
            .state(state)
            .build();
        verify(caseDetailsConverter).convertToMap(updatedCcdCase);

        Assertions.assertEquals(state, response.getData().get("state"));
    }

    @Test
    void shouldReturnJudgmentDecideAmountIfCtscEnabled() {

        handler = new InterlocutoryJudgmentCallbackHandler(caseDetailsConverter, true);

        String state = ClaimState.JUDGMENT_DECIDE_AMOUNT.getValue();
        when(caseDetailsConverter.convertToMap(ccdCase)).thenReturn(ImmutableMap.of("state", state));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CCDCase updatedCcdCase = CCDCase.builder()
            .state(state)
            .build();
        verify(caseDetailsConverter).convertToMap(updatedCcdCase);

        Assertions.assertEquals(state, response.getData().get("state"));
    }
}
