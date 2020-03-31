package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFER_TO_JUDGE_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFER_TO_JUDGE_BY_DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class ReferToJudgeCallbackHandlerTest {
    private ReferToJudgeCallbackHandler handler;

    private CallbackParams params;

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
        }

        @Test
        void shouldReturnOpenStateForReferToJudgeByClaimantIfCtscDisabled() {

            handler = new ReferToJudgeCallbackHandler(false);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertEquals(ClaimState.OPEN.getValue(), response.getData().get("state"));
        }

        @Test
        void shouldReturnRedeterminationRequestedStateForReferToJudgeByClaimantIfCtscEnabled() {

            handler = new ReferToJudgeCallbackHandler(true);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertEquals(ClaimState.REDETERMINATION_REQUESTED.getValue(),
                response.getData().get("state"));
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
        }

        @Test
        void shouldReturnOpenStateForReferToJudgeByDefendantIfCtscDisabled() {

            handler = new ReferToJudgeCallbackHandler(false);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertEquals(ClaimState.OPEN.getValue(), response.getData().get("state"));
        }

        @Test
        void shouldReturnRedeterminationRequestedStateForReferToJudgeByDefendantIfCtscEnabled() {

            handler = new ReferToJudgeCallbackHandler(true);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertEquals(ClaimState.REDETERMINATION_REQUESTED.getValue(),
                response.getData().get("state"));
        }
    }
}
