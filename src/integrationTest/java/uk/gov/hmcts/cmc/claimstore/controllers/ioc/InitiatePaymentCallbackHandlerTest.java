package uk.gov.hmcts.cmc.claimstore.controllers.ioc;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INITIATE_CLAIM_PAYMENT_CITIZEN;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.OPEN;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.SUCCESS;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api",
        "fees.api.url=http://fees-api",
        "payments.api.url=http://payments-api"
    }
)
public class InitiatePaymentCallbackHandlerTest extends BaseMockSpringTest {

    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    private static final long CASE_ID = 42L;
    private static final String NEXT_URL = "http://nexturl.test";

    private Payment payment;

    @Before
    public void setUp() {
        payment = Payment.builder()
            .amount(TEN)
            .reference("reference2")
            .status(SUCCESS)
            .dateCreated("2017-12-03")
            .nextUrl(NEXT_URL)
            .build();
        given(paymentsService
            .createPayment(
                eq(AUTHORISATION_TOKEN),
                any(Claim.class)))
            .willReturn(payment);

        UserDetails userDetails = SampleUserDetails.builder().withRoles("citizen").build();
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(userDetails);
    }

    @Test
    public void shouldStorePaymentDetailsBeforeSubmittingEvent() throws Exception {
        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isOk())
            .andReturn();
        Map<String, Object> responseData = jsonMappingHelper.deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        assertThat(responseData).hasSize(46);
        assertThat(responseData).contains(
            entry("channel", "CITIZEN"),
            entry("paymentAmount", "1000"),
            entry("paymentReference", payment.getReference()),
            entry("paymentStatus", payment.getStatus().toString()),
            entry("paymentDateCreated", payment.getDateCreated()),
            entry("paymentNextUrl", NEXT_URL)
        );
    }

    private ResultActions makeRequest(String callbackType) throws Exception {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .data(caseDetailsConverter.convertToMap(
                    SampleData.getCCDCitizenCaseWithoutPayment()))
                .state(OPEN.getValue())
                .build())
            .build();

        return webClient
            .perform(post("/cases/callbacks/" + callbackType)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMappingHelper.toJson(callbackRequest))
            );
    }

    @Test
    public void shouldReturnErrorForUnknownCallback() throws Exception {
        MvcResult mvcResult = makeRequest("not-a-real-callback")
            .andExpect(status().isBadRequest())
            .andReturn();
        assertThat(mvcResult.getResolvedException())
            .isInstanceOfAny(CallbackException.class);
    }
}
