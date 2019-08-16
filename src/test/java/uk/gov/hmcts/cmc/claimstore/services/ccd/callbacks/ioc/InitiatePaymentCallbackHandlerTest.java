package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INITIATE_CLAIM_PAYMENT_CITIZEN;

public class InitiatePaymentCallbackHandlerTest {

    private static final String BEARER_TOKEN = "Bearer let me in";

    private InitiatePaymentCallbackHandler handler;

    @Before
    public void setUp() {
        handler = new InitiatePaymentCallbackHandler();
    }

    @Test
    public void shouldStoreNextUrlAndCaseId() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
            .caseDetails(CaseDetails.builder()
                .id(3L)
                .data(ImmutableMap.of("data", "existingData"))
                .build())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handler.handle(callbackParams);

        assertThat(response.getData()).contains(
            entry("data", "existingData"),
            entry("id", 3L),
            entry("paymentNextUrl", "http://payment_next_url.test")
        );
    }

}
