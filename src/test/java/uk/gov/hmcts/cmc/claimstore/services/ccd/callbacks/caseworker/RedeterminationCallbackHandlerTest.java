package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFER_TO_JUDGE_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFER_TO_JUDGE_BY_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class RedeterminationCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private RedeterminationCallbackHandler handler;

    private CallbackParams params;

    private CCDCase ccdCase;

    @Nested
    @DisplayName("State Change for referred to judge by claimant")
    class ReferToJudgeByClaimant {
        @BeforeEach
        void setUp() {
            CallbackRequest callbackRequest = CallbackRequest
                .builder()
                .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
                .eventId(REFER_TO_JUDGE_BY_CLAIMANT.getValue())
                .build();

            params = CallbackParams.builder()
                .request(callbackRequest)
                .type(CallbackType.ABOUT_TO_SUBMIT).build();

            ccdCase = CCDCase.builder()
                .build();
            when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);
        }

        @Test
        void shouldReturnOpenStateForReferToJudgeByClaimantIfCtscDisabled() {

            handler = new RedeterminationCallbackHandler(caseDetailsConverter, false);

            String state = ClaimState.OPEN.getValue();
            when(caseDetailsConverter.convertToMap(ccdCase)).thenReturn(ImmutableMap.of("state", state));

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CCDCase updatedCcdCase = CCDCase.builder()
                .state(state)
                .build();
            verify(caseDetailsConverter).convertToMap(updatedCcdCase);

            Assertions.assertEquals(state, response.getData().get("state"));
        }

        @Test
        void shouldReturnRedeterminationRequestedStateForReferToJudgeByClaimantIfCtscEnabled() {

            handler = new RedeterminationCallbackHandler(caseDetailsConverter, true);

            String state = ClaimState.REDETERMINATION_REQUESTED.getValue();
            when(caseDetailsConverter.convertToMap(ccdCase)).thenReturn(ImmutableMap.of("state", state));

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CCDCase updatedCcdCase = CCDCase.builder()
                .state(state)
                .build();
            verify(caseDetailsConverter).convertToMap(updatedCcdCase);

            Assertions.assertEquals(state, response.getData().get("state"));
        }
    }

    @Nested
    @DisplayName("State Change for referred to judge by defendant")
    class ReferToJudgeByDefendant {
        @BeforeEach
        void setUp() {
            CallbackRequest callbackRequest = CallbackRequest
                .builder()
                .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
                .eventId(REFER_TO_JUDGE_BY_DEFENDANT.getValue())
                .build();

            params = CallbackParams.builder()
                .request(callbackRequest)
                .type(CallbackType.ABOUT_TO_SUBMIT).build();

            ccdCase = CCDCase.builder()
                .build();
            when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);
        }

        @Test
        void shouldReturnOpenStateForReferToJudgeByDefendantIfCtscDisabled() {

            handler = new RedeterminationCallbackHandler(caseDetailsConverter, false);

            String state = ClaimState.OPEN.getValue();
            when(caseDetailsConverter.convertToMap(ccdCase)).thenReturn(ImmutableMap.of("state", state));

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CCDCase updatedCcdCase = CCDCase.builder()
                .state(state)
                .build();
            verify(caseDetailsConverter).convertToMap(updatedCcdCase);

            Assertions.assertEquals(state, response.getData().get("state"));
        }

        @Test
        void shouldReturnRedeterminationRequestedStateForReferToJudgeByDefendantIfCtscEnabled() {

            handler = new RedeterminationCallbackHandler(caseDetailsConverter, true);

            String state = ClaimState.REDETERMINATION_REQUESTED.getValue();
            when(caseDetailsConverter.convertToMap(ccdCase)).thenReturn(ImmutableMap.of("state", state));

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CCDCase updatedCcdCase = CCDCase.builder()
                .state(state)
                .build();
            verify(caseDetailsConverter).convertToMap(updatedCcdCase);

            Assertions.assertEquals(state, response.getData().get("state"));
        }
    }
}
