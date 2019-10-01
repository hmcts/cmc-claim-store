package uk.gov.hmcts.cmc.claimstore.controllers;

import feign.FeignException;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ioc.CreatePaymentResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INITIATE_CLAIM_PAYMENT_CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "document_management.url=false",
        "feature_toggles.ccd_async_enabled=false",
        "feature_toggles.ccd_enabled=true",
        "feature_toggles.async_event_operations_enabled=true",
        "payments.api.url=http://payments-api",
        "fees.api.url=http://fees-api"
    }
)
public class InitiatePaymentTest extends BaseSaveTest {

    @Test
    public void shouldReturnNewlyCreatedClaim() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimant();

        given(coreCaseDataApi.startForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(IGNORE_WARNING),
            any()
            )
        ).willReturn(successfulCoreCaseDataStoreSubmitResponse());

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        MvcResult result = makeInitiatePaymentRequest(claimData, AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(coreCaseDataApi)
            .startForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
            );

        verify(coreCaseDataApi)
            .submitForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(IGNORE_WARNING),
                any()
            );

        assertThat(deserializeObjectFrom(result, CreatePaymentResponse.class))
            .extracting(CreatePaymentResponse::getNextUrl)
            .isEqualTo("http://nexturl.test");
    }

    @Test
    public void shouldFailIssuingClaimEvenWhenCCDStoreFailsToStartEvent() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimant();

        given(coreCaseDataApi.startForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
            )
        ).willThrow(FeignException.class);

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        MvcResult result = makeInitiatePaymentRequest(claimData, AUTHORISATION_TOKEN)
            .andExpect(status().isInternalServerError())
            .andReturn();

        assertThat(result.getResolvedException().getMessage())
            .isEqualTo("Failed creating a payment in CCD store for claim with "
                + "external id " + claimData.getExternalId() + " on event INITIATE_CLAIM_PAYMENT_CITIZEN");
    }

    @Test
    public void shouldIssueClaimEvenWhenCCDStoreFailsToSubmitEvent() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimant();

        given(coreCaseDataApi.startForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(IGNORE_WARNING),
            any()
            )
        ).willThrow(FeignException.class);

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        MvcResult result = makeInitiatePaymentRequest(claimData, AUTHORISATION_TOKEN)
            .andExpect(status().isInternalServerError())
            .andReturn();

        assertThat(result.getResolvedException().getMessage())
            .isEqualTo("Failed creating a payment in CCD store for claim with "
                + "external id " + claimData.getExternalId() + " on event INITIATE_CLAIM_PAYMENT_CITIZEN");
    }

    private ResultActions makeInitiatePaymentRequest(ClaimData claimData, String authorization) throws Exception {
        return webClient
            .perform(post("/claims/" + USER_ID + "/initiate-citizen-payment")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .content(jsonMapper.toJson(claimData))
            );
    }
}
