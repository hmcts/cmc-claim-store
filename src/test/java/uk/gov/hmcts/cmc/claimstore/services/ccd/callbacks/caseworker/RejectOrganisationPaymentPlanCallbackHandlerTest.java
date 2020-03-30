package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REJECT_ORGANISATION_PAYMENT_PLAN;

@ExtendWith(MockitoExtension.class)
class RejectOrganisationPaymentPlanCallbackHandlerTest {

    private RejectOrganisationPaymentPlanCallbackHandler handler;

    private CallbackParams params;

    @BeforeEach
    void setUp() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(REJECT_ORGANISATION_PAYMENT_PLAN.getValue())
            .build();

        params = CallbackParams.builder()
            .request(callbackRequest)
            .type(CallbackType.ABOUT_TO_SUBMIT).build();
    }

    @Test
    void shouldReturnOpenStateIfCtscNotEnabled() {

        handler = new RejectOrganisationPaymentPlanCallbackHandler(false);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        Assertions.assertEquals(ClaimState.OPEN.getValue(), response.getData().get("state"));
    }

    @Test
    void shouldReturnJudgmentDecideAmountIfCtscEnabled() {

        handler = new RejectOrganisationPaymentPlanCallbackHandler(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        Assertions.assertEquals(ClaimState.JUDGMENT_DECIDE_AMOUNT.getValue(), response.getData().get("state"));
    }
}
