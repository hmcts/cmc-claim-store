package uk.gov.hmcts.cmc.claimstore.controllers.ioc;

import com.github.tomakehurst.wiremock.http.MimeType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc.PaymentsService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CITIZEN_CLAIM;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getAmountBreakDown;
import static uk.gov.hmcts.cmc.claimstore.services.CallbackHandlerFactoryTest.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.OPEN;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.FAILED;
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
    private static final LocalDate ISSUE_DATE = now();
    private static final LocalDate RESPONSE_DEADLINE = ISSUE_DATE.plusDays(14);

    @MockBean
    private PaymentsService paymentsService;

    @MockBean
    protected ResponseDeadlineCalculator responseDeadlineCalculator;

    @MockBean
    protected IssueDateCalculator issueDateCalculator;

    @MockBean
    private EventProducer eventProducer;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    private Payment.PaymentBuilder paymentBuilder;

    private Payment payment;

    @Before
    public void setUp() {
        paymentBuilder = Payment.builder()
            .amount(TEN)
            .reference("reference2")
            .dateCreated("2017-12-03")
            .nextUrl(NEXT_URL);

        given(referenceNumberRepository.getReferenceNumberForCitizen()).willReturn(REFERENCE_NO);
        given(responseDeadlineCalculator.calculateResponseDeadline(any())).willReturn(RESPONSE_DEADLINE);
        given(issueDateCalculator.calculateIssueDay(any())).willReturn(ISSUE_DATE);

        UserDetails userDetails = SampleUserDetails.builder().withRoles("citizen").build();
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(userDetails);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldAddFieldsOnCaseWhenCallbackIsSuccessful() throws Exception {
        payment = paymentBuilder.status(SUCCESS).build();

        given(paymentsService.retrievePayment(eq(AUTHORISATION_TOKEN), any(Claim.class))).willReturn(payment);

        MvcResult mvcResult = makeRequestAndRespondWithSuccess(CallbackType.ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isOk())
            .andReturn();
        Map<String, Object> responseData = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        List<Map<String, Object>> respondents = (List<Map<String, Object>>) responseData.get("respondents");
        Map<String, Object> defendant = (Map<String, Object>) respondents.get(0).get("value");

        assertThat(responseData).contains(
            entry("paymentStatus", SUCCESS.toString()),
            entry("issuedOn", ISSUE_DATE.toString()),
            entry("previousServiceCaseReference", REFERENCE_NO)
        );

        assertThat(defendant).contains(entry("responseDeadline", RESPONSE_DEADLINE.toString()));
    }

    @Test
    public void shouldAddFieldsOnCaseWhenCallbackIsSuccessfulButWithErrors() throws Exception {
        payment = paymentBuilder.status(FAILED).build();

        given(paymentsService.retrievePayment(eq(AUTHORISATION_TOKEN), any(Claim.class))).willReturn(payment);

        MvcResult mvcResult = makeRequestAndRespondWithError(CallbackType.ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isOk())
            .andReturn();

        List<String> responseData = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getErrors();

        assertThat(responseData).contains("Payment not successful");
    }

    @Test
    public void shouldCallClaimIssuePostOperationsWhenSubmittedCallbackIsSuccessful() throws Exception {
        given(userService.getUser(anyString())).willReturn(new User(BEARER_TOKEN, SampleUserDetails.builder().build()));

        MvcResult mvcResult = makeRequestAndRespondWithError(CallbackType.SUBMITTED.getValue())
            .andExpect(status().isOk())
            .andReturn();

        SubmittedCallbackResponse response = deserializeObjectFrom(
            mvcResult,
            SubmittedCallbackResponse.class
        );

        verify(eventProducer, once()).createClaimCreatedEvent(any(Claim.class), anyString(), anyString());

        assertThat(response.getConfirmationBody()).isNull();
    }

    private ResultActions makeRequestAndRespondWithSuccess(String callbackType) throws Exception {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(caseDetailsConverter.convertToMap(SampleData.getCCDCitizenCase(getAmountBreakDown())))
            .state(OPEN.getValue())
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CREATE_CITIZEN_CLAIM.getValue())
            .caseDetails(caseDetails)
            .build();

        return webClient.perform(post("/cases/callbacks/" + callbackType)
            .header(HttpHeaders.CONTENT_TYPE, MimeType.JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
            .content(jsonMapper.toJson(callbackRequest))
        );
    }

    private ResultActions makeRequestAndRespondWithError(String callbackType) throws Exception {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(caseDetailsConverter.convertToMap(
                SampleData.getCCDCitizenCase(
                    getAmountBreakDown()).toBuilder().paymentStatus(FAILED.toString()).build()))
            .state(OPEN.getValue())
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CREATE_CITIZEN_CLAIM.getValue())
            .caseDetails(caseDetails)
            .build();

        return webClient.perform(post("/cases/callbacks/" + callbackType)
            .header(HttpHeaders.CONTENT_TYPE, MimeType.JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
            .content(jsonMapper.toJson(callbackRequest))
        );
    }
}
