package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
class DirectionsQuestionnaireDeadlineCallbackHandlerTest {

    private DirectionsQuestionnaireDeadlineCallbackHandler handler;

    @Test
    void shouldReturnOpenStateIfStaffEmailsEnabled() {
        handler = new DirectionsQuestionnaireDeadlineCallbackHandler(true);

        CallbackParams params = CallbackParams.builder().type(CallbackType.ABOUT_TO_SUBMIT).build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        Assertions.assertEquals(ClaimState.OPEN.getValue(), response.getData().get("state"));
    }

    @Test
    void shouldReturnReadyForPaperDQStateIfStaffEmailsNotEnabled() {
        handler = new DirectionsQuestionnaireDeadlineCallbackHandler(false);

        CallbackParams params = CallbackParams.builder().type(CallbackType.ABOUT_TO_SUBMIT).build();
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        Assertions.assertEquals(ClaimState.READY_FOR_PAPER_DQ.getValue(), response.getData().get("state"));
    }
}
