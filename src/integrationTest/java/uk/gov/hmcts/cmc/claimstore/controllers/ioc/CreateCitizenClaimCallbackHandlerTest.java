package uk.gov.hmcts.cmc.claimstore.controllers.ioc;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc.PaymentsService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CITIZEN_CLAIM;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getAmountBreakDown;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.OPEN;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.SUCCESS;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api",
        "fees.api.url=http://fees-api",
        "payments.api.url=http://payments-api"
    }
)
public class CreateCitizenClaimCallbackHandlerTest extends MockSpringTest {
    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    private static final long CASE_ID = 42L;
    private static final String NEXT_URL = "http://nexturl.test";
    private static final String REFERENCE_NO = "000MC001";

    @MockBean
    private PaymentsService paymentsService;
    @Autowired
    private CaseDetailsConverter caseDetailsConverter;
    @Autowired
    private CaseMapper caseMapper;

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

        given(referenceNumberRepository.getReferenceNumberForCitizen()).willReturn(REFERENCE_NO);

        given(paymentsService
            .retrievePayment(
                eq(AUTHORISATION_TOKEN),
                any(Claim.class)))
            .willReturn(payment);

        UserDetails userDetails = SampleUserDetails.builder().withRoles("citizen").build();
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(userDetails);
    }

    @Test
    public void shouldAddFieldsOnCaseWhenCallbackIsSuccessful() throws Exception {

        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isOk())
            .andReturn();
        Map<String, Object> responseData = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        List<Map<String, Object>> respondents = (List<Map<String, Object>>) responseData.get("respondents");
        Map<String, Object> defendant = (Map<String, Object>) respondents.get(0).get("value");

        assertThat(responseData).contains(
            entry("paymentStatus", responseData.get("paymentStatus")),
            entry("issuedOn", responseData.get("issuedOn"))
        );

        assertThat(defendant).contains(entry("responseDeadline", defendant.get("responseDeadline")));
    }

    private ResultActions makeRequest(String callbackType) throws Exception {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(caseDetailsConverter.convertToMap(
                SampleData.getCCDCitizenCase(getAmountBreakDown())))
            .state(OPEN.getValue())
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CREATE_CITIZEN_CLAIM.getValue())
            .caseDetails(caseDetails)
            .build();

        return webClient
            .perform(post("/cases/callbacks/" + callbackType)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMapper.toJson(callbackRequest))
            );
    }
}
