package uk.gov.hmcts.cmc.claimstore.controllers.ioc;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc.PaymentsService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.payments.client.models.LinkDto;
import uk.gov.hmcts.reform.payments.client.models.LinksDto;
import uk.gov.hmcts.reform.payments.client.models.PaymentDto;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api",
        "fees.api.url=http://fees-api",
        "payments.api.url=http://payments-api"
    }
)
public class InitiatePaymentCallbackHandlerTest extends MockSpringTest {

    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    private static final long CASE_ID = 42L;
    private static final String NEXT_URL = "http://nexturl.test";

    @MockBean
    private PaymentsService paymentsService;
    @Autowired
    private CaseDetailsConverter caseDetailsConverter;
    @Autowired
    private CaseMapper caseMapper;

    private PaymentDto payment;

    @Before
    public void setUp() throws URISyntaxException {
        payment = PaymentDto.builder()
            .amount(BigDecimal.TEN)
            .reference("reference")
            .status("status")
            .dateCreated(OffsetDateTime.parse("2017-02-03T10:15:30+01:00"))
            .links(LinksDto.builder()
                .nextUrl(
                    LinkDto.builder()
                        .href(new URI(NEXT_URL))
                        .build()
                ).build())
            .build();
        given(paymentsService
            .createPayment(
                eq(AUTHORISATION_TOKEN),
                any(Claim.class)))
            .willReturn(payment);
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

        assertThat(responseData).contains(
            entry("channelType", "CITIZEN"),
            entry("paymentAmount", "1000"),
            entry("paymentReference", payment.getReference()),
            entry("paymentStatus", payment.getStatus()),
            entry("paymentDateCreated", payment.getDateCreated().toLocalDate().toString()),
            entry("paymentNextUrl", NEXT_URL)
        );
    }

    private ResultActions makeRequest(String callbackType) throws Exception {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .data(caseDetailsConverter.convertToMap(
                    SampleData.getCCDCitizenCaseWithoutPayment()))
                .build())
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
