package uk.gov.hmcts.cmc.claimstore.controllers.ioc;

import com.google.common.collect.ImmutableList;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserInfo;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ioc.CreatePaymentResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INITIATE_CLAIM_PAYMENT_CITIZEN;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getAmountBreakDown;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDCitizenCase;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.AWAITING_CITIZEN_PAYMENT;

@TestPropertySource(
    properties = {
        "document_management.url=false",
        "payments.api.url=http://payments-api",
        "fees.api.url=http://fees-api"
    }
)
public class InitiatePaymentTest extends BaseMockSpringTest {

    @Autowired
    protected ResponseDeadlineCalculator responseDeadlineCalculator;
    @Autowired
    protected IssueDateCalculator issueDateCalculator;

    @MockBean
    protected EmailService emailService;

    @BeforeEach
    public void setup() {
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder()
            .roles(ImmutableList.of(Role.CITIZEN.getRole()))
            .uid(SampleClaim.USER_ID)
            .sub(SampleClaim.SUBMITTER_EMAIL)
            .build());
        given(userService.getUser(BEARER_TOKEN)).willReturn(new User(BEARER_TOKEN, USER_DETAILS));
    }

    @Test
    public void shouldReturnNewlyCreatedClaim() throws Exception {
        given(coreCaseDataApi.startForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        CCDCase data = getCCDCitizenCase(getAmountBreakDown());
        data.setPaymentNextUrl("http://nexturl.test");

        given(coreCaseDataApi.submitForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(IGNORE_WARNING),
            any()
            )
        ).willReturn(CaseDetails.builder()
            .id(3L)
            .state(AWAITING_CITIZEN_PAYMENT.getValue())
            .data(caseDetailsConverter.convertToMap(data))
            .build());

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        MvcResult result = makeInitiatePaymentRequest(SampleClaimData.submittedByClaimant(), BEARER_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(coreCaseDataApi)
            .startForCitizen(
                eq(BEARER_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
            );

        verify(coreCaseDataApi)
            .submitForCitizen(
                eq(BEARER_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(IGNORE_WARNING),
                any()
            );

        assertThat(jsonMappingHelper.deserializeObjectFrom(result, CreatePaymentResponse.class))
            .extracting(CreatePaymentResponse::getNextUrl)
            .isEqualTo("http://nexturl.test");
    }

    @Test
    public void shouldFailCreatingPaymentWhenCCDStoreFailsToStartEvent() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimant();

        given(coreCaseDataApi.startForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
            )
        ).willThrow(FeignException.class);

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        MvcResult result = makeInitiatePaymentRequest(claimData, BEARER_TOKEN)
            .andExpect(status().isFailedDependency())
            .andReturn();

        assertThat(result.getResolvedException())
            .hasMessage("Failed creating a payment in CCD store for claim with "
                + "external id " + claimData.getExternalId() + " on event INITIATE_CLAIM_PAYMENT_CITIZEN");
    }

    @Test
    public void shouldFailCreatingPaymentWhenCCDStoreFailsToSubmitEvent() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimant();

        given(coreCaseDataApi.startForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitForCitizen(
            eq(BEARER_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(IGNORE_WARNING),
            any()
            )
        ).willThrow(FeignException.class);

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        MvcResult result = makeInitiatePaymentRequest(claimData, BEARER_TOKEN)
            .andExpect(status().isFailedDependency())
            .andReturn();

        assertThat(result.getResolvedException())
            .hasMessage("Failed creating a payment in CCD store for claim with "
                + "external id " + claimData.getExternalId() + " on event INITIATE_CLAIM_PAYMENT_CITIZEN");
    }

    private ResultActions makeInitiatePaymentRequest(ClaimData claimData, String authorization) throws Exception {
        return webClient
            .perform(post("/claims/initiate-citizen-payment")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .content(jsonMappingHelper.toJson(claimData))
            );
    }
}
