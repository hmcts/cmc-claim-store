package uk.gov.hmcts.cmc.claimstore.controllers.ioc;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc.PaymentsService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.INITIATED;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.SUCCESS;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api",
        "fees.api.url=http://fees-api",
        "payments.api.url=http://payments-api"
    }
)
public class ResumePaymentCallbackHandlerTest extends MockSpringTest {

    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    private static final String NEXT_URL = "http://nexturl.test";

    @MockBean
    private PaymentsService paymentsService;
    @Autowired
    private CaseDetailsConverter caseDetailsConverter;
    @Autowired
    private CaseMapper caseMapper;

    @Test
    public void shouldStorePaymentDetailsBeforeSubmittingEvent() throws Exception {
        Payment payment = Payment.builder()
            .amount(BigDecimal.TEN)
            .reference("reference")
            .status(SUCCESS)
            .dateCreated("2017-12-03+01:00")
            .nextUrl(NEXT_URL)
            .build();
        given(paymentsService
            .retrievePayment(
                eq(AUTHORISATION_TOKEN),
                any(Claim.class)))
            .willReturn(payment);

        Payment newPayment = Payment.builder()
            .amount(BigDecimal.valueOf(25))
            .reference("reference2")
            .status(INITIATED)
            .dateCreated("2017-12-03+01:00")
            .nextUrl(NEXT_URL)
            .build();
        given(paymentsService
            .createPayment(
                eq(AUTHORISATION_TOKEN),
                any(Claim.class)))
            .willReturn(newPayment);

        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isOk())
            .andReturn();
        Map<String, Object> responseData = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        assertThat(responseData).contains(
            entry("channel", "CITIZEN"),
            entry("paymentAmount", "2500"),
            entry("paymentReference", newPayment.getReference()),
            entry("paymentStatus", newPayment.getStatus()),
            entry("paymentDateCreated", "2017-12-03"),
            entry("paymentNextUrl", NEXT_URL)
        );
    }

    @Test
    public void shouldReturnPaymentDetailsIfPaymentIsSuccessful() throws Exception {
        Payment payment = Payment.builder()
            .amount(BigDecimal.TEN)
            .reference("reference")
            .status(SUCCESS)
            .dateCreated("2017-12-03+01:00")
            .nextUrl(NEXT_URL)
            .build();
        given(paymentsService
            .retrievePayment(
                eq(AUTHORISATION_TOKEN),
                any(Claim.class)))
            .willReturn(payment);

        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isOk())
            .andReturn();
        Map<String, Object> responseData = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        assertThat(responseData).contains(
            entry("paymentAmount", "1000"),
            entry("paymentReference", payment.getReference()),
            entry("paymentStatus", payment.getStatus()),
            entry("paymentDateCreated", "2017-12-03"),
            entry("paymentNextUrl", NEXT_URL)
        );
    }

    @Test
    public void shouldReturnPaymentDetailsIfPaymentIsInitiated() throws Exception {
        Payment payment = Payment.builder()
            .amount(BigDecimal.TEN)
            .reference("reference")
            .status(INITIATED)
            .dateCreated("2017-12-03+01:00")
            .nextUrl(NEXT_URL)
            .build();
        given(paymentsService
            .retrievePayment(
                eq(AUTHORISATION_TOKEN),
                any(Claim.class)))
            .willReturn(payment);

        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isOk())
            .andReturn();
        Map<String, Object> responseData = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        assertThat(responseData).contains(
            entry("paymentAmount", "1000"),
            entry("paymentReference", payment.getReference()),
            entry("paymentStatus", payment.getStatus()),
            entry("paymentDateCreated", "2017-12-03"),
            entry("paymentNextUrl", NEXT_URL)
        );
    }

    private ResultActions makeRequest(String callbackType) throws Exception {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.RESUME_CLAIM_PAYMENT_CITIZEN.getValue())
            .caseDetails(successfulCoreCaseDataStoreSubmitResponse())
            .build();

        return webClient
            .perform(post("/cases/callbacks/" + callbackType)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMapper.toJson(callbackRequest))
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
