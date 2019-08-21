package uk.gov.hmcts.cmc.claimstore.controllers.ioc;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.InitiatePaymentCaseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.MoneyMapper;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc.PaymentsService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInitiatePaymentRequest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.payments.client.models.NavigationLink;
import uk.gov.hmcts.reform.payments.client.models.NavigationLinks;
import uk.gov.hmcts.reform.payments.client.models.Payment;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api"
    }
)
public class InitiatePaymentCallbackHandlerTest extends MockSpringTest {

    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    private static final long CASE_ID = 42L;
    private static final String NEXT_URL = "http://nexturl.test";

    @MockBean
    private PaymentsService paymentsService;
    @MockBean
    private InitiatePaymentCaseMapper initiatePaymentCaseMapper;
    @MockBean
    private MoneyMapper moneyMapper;

    private Payment payment;

    @Before
    public void setUp() throws URISyntaxException {
        payment = Payment.builder()
            .amount(BigDecimal.TEN)
            .reference("reference")
            .status("status")
            .dateCreated("2019-10-10")
            .links(NavigationLinks.builder()
                .nextUrl(
                    NavigationLink.builder()
                        .href(new URI(NEXT_URL))
                        .build()
                ).build())
            .build();
        given(paymentsService
            .makePayment(
                eq(AUTHORISATION_TOKEN),
                any(CCDCase.class),
                any(BigDecimal.class))).willReturn(payment);
        given(moneyMapper.to(any(BigDecimal.class))).willReturn("amount");
        given(initiatePaymentCaseMapper.from(any(CCDCase.class)))
            .willReturn(SampleInitiatePaymentRequest.builder().build());
    }

    @Test
    public void shouldStorePaymentDetailsBeforeSubmittingEvent() throws Exception {
        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isOk())
            .andReturn();
        Map<String, Object> responseData = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        assertThat(responseData).hasSize(7);
        assertThat(responseData).contains(
            entry("id", 42),
            entry("data", "existingData"),
            entry("paymentAmount", "amount"),
            entry("paymentReference", payment.getReference()),
            entry("paymentStatus", payment.getStatus()),
            entry("paymentDateCreated", payment.getDateCreated()),
            entry("paymentNextUrl", NEXT_URL)
        );
    }

    private ResultActions makeRequest(String callbackType) throws Exception {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(ImmutableMap.of("data", "existingData"))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
            .caseDetails(caseDetails)
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
