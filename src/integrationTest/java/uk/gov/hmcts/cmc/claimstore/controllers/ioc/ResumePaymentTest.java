package uk.gov.hmcts.cmc.claimstore.controllers.ioc;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.cmc.domain.models.ioc.CreatePaymentResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RESUME_CLAIM_PAYMENT_CITIZEN;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.AWAITING_CITIZEN_PAYMENT;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.INITIATED;
import static uk.gov.hmcts.cmc.domain.models.PaymentStatus.SUCCESS;

@TestPropertySource(
    properties = {
        "document_management.url=false",
        "payments.returnUrlPattern=http://returnUrl.test/blah/%s/test",
        "payments.api.url=http://payments-api",
        "fees.api.url=http://fees-api"
    }
)
public class ResumePaymentTest extends BaseMockSpringTest {

    private static final Long CASE_ID = 42L;
    private static final String NEXT_URL = "http://nexturl.test";
    private static final String RETURN_URL = "http://returnUrl.test/blah/%s/test";
    private static final String PAYMENT_REFERENCE = "reference";

    @Before
    public void before() {
        UserDetails userDetails = SampleUserDetails.builder()
            .withRoles("citizen")
            .withUserId(SUBMITTER_ID)
            .build();

        User user = new User(BEARER_TOKEN, userDetails);
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(userDetails);
        given(userService.getUser(BEARER_TOKEN)).willReturn(user);
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void shouldReturnReturnUrlIfPaymentIsSuccessful() throws Exception {
        Claim claim = claimWithPaymentStatus(SUCCESS);
        mockCcdCallsFor(claim);

        MvcResult result = makeResumePaymentRequest(claim.getClaimData(), BEARER_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(paymentsService, never()).createPayment(eq(BEARER_TOKEN), any(Claim.class));

        assertThat(jsonMappingHelper.deserializeObjectFrom(result, CreatePaymentResponse.class))
            .extracting(CreatePaymentResponse::getNextUrl)
            .isEqualTo(String.format(RETURN_URL, claim.getExternalId()));
    }

    @Test
    public void shouldReturnNextUrlIfPaymentIsNotSuccessful() throws Exception {
        Claim claim = claimWithPaymentStatus(INITIATED);
        mockCcdCallsFor(claim);

        MvcResult result = makeResumePaymentRequest(claim.getClaimData(), BEARER_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(paymentsService, never()).createPayment(eq(BEARER_TOKEN), any(Claim.class));

        assertThat(jsonMappingHelper.deserializeObjectFrom(result, CreatePaymentResponse.class))
            .extracting(CreatePaymentResponse::getNextUrl)
            .isEqualTo(NEXT_URL);
    }

    private void mockCcdCallsFor(Claim claim) {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .state(AWAITING_CITIZEN_PAYMENT.getValue())
            .data(caseDetailsConverter.convertToMap(caseMapper.to(claim)))
            .build();

        when(coreCaseDataApi.searchForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(SUBMITTER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(searchCriteria(claim.getExternalId()))
            )
        ).thenReturn(ImmutableList.of(caseDetails));

        given(coreCaseDataApi.startEventForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(SUBMITTER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(String.valueOf(CASE_ID)),
            eq(RESUME_CLAIM_PAYMENT_CITIZEN.getValue())
            )
        ).willReturn(StartEventResponse.builder()
            .eventId(RESUME_CLAIM_PAYMENT_CITIZEN.getValue())
            .caseDetails(caseDetails)
            .token("atoken")
            .build());

        given(coreCaseDataApi.submitEventForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(SUBMITTER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(String.valueOf(CASE_ID)),
            eq(IGNORE_WARNING),
            any()
            )
        ).willReturn(caseDetails);
    }

    private Claim claimWithPaymentStatus(PaymentStatus status) {
        UUID externalId = UUID.randomUUID();

        Payment payment = Payment.builder()
            .amount(BigDecimal.TEN)
            .reference(PAYMENT_REFERENCE)
            .status(status)
            .dateCreated("2017-12-03+01:00")
            .nextUrl(NEXT_URL)
            .build();
        return SampleClaim.getDefault()
            .toBuilder()
            .externalId(externalId.toString())
            .submitterId(SUBMITTER_ID)
            .claimData(SampleClaimData.submittedByClaimant()
                .toBuilder()
                .externalId(externalId)
                .payment(payment)
                .build())
            .build();
    }

    private ResultActions makeResumePaymentRequest(ClaimData claimData, String authorization) throws Exception {
        return webClient
            .perform(put("/claims/resume-citizen-payment")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .content(jsonMappingHelper.toJson(claimData))
            );
    }
}
