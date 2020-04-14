package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@ExtendWith(MockitoExtension.class)
class DirectionsQuestionnaireDeadlineCallbackHandlerTest {

    private DirectionsQuestionnaireDeadlineCallbackHandler handler;
    private CallbackParams params;

    @BeforeEach
    void setUp() {
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(ImmutableMap.of()).build())
            .build();

        params = CallbackParams.builder()
           .type(CallbackType.ABOUT_TO_SUBMIT)
           .request(request)
           .build();

    }

    @Test
    void shouldReturnOpenStateIfCtscNotEnabled() {
        handler = new DirectionsQuestionnaireDeadlineCallbackHandler(false);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        Assertions.assertEquals(ClaimState.OPEN.getValue(), response.getData().get("state"));
    }

    @Test
    void shouldReturnReadyForPaperDQStateIfCtscEnabled() {
        handler = new DirectionsQuestionnaireDeadlineCallbackHandler(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        Assertions.assertEquals(ClaimState.READY_FOR_PAPER_DQ.getValue(), response.getData().get("state"));
    }
}
