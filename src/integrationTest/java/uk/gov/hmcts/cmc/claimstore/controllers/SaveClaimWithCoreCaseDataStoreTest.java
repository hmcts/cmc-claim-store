package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableList;
import feign.FeignException;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitRepresentativeResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulPrePaymentCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "document_management.url=false",
        "core_case_data.api.url=http://core-case-data-api",
        "feature_toggles.ccd_async_enabled=false",
        "feature_toggles.ccd_enabled=true"
    }
)
public class SaveClaimWithCoreCaseDataStoreTest extends BaseSaveTest {

    @Test
    public void shouldStoreRepresentedClaimIntoCCD() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();

        CaseDetails prepaymentCaseDetails = successfulPrePaymentCoreCaseDataStoreSubmitResponse();
        given(coreCaseDataApi.searchForCaseworker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            anyMap()
            )
        ).willReturn(ImmutableList.of(prepaymentCaseDetails))
            .willReturn(ImmutableList.of(successfulCoreCaseDataStoreSubmitResponse()));

        given(coreCaseDataApi.startEventForCaseWorker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(prepaymentCaseDetails.getId().toString()),
            eq(SUBMIT_POST_PAYMENT)
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitEventForCaseWorker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(prepaymentCaseDetails.getId().toString()),
            eq(IGNORE_WARNING),
            any()
            )
        ).willReturn(successfulCoreCaseDataStoreSubmitRepresentativeResponse());

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        makeIssueClaimRequest(claimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(coreCaseDataApi)
            .startEventForCaseWorker(
                eq(SOLICITOR_AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(prepaymentCaseDetails.getId().toString()),
                eq(SUBMIT_POST_PAYMENT)
            );

        verify(coreCaseDataApi)
            .submitEventForCaseWorker(
                eq(SOLICITOR_AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(prepaymentCaseDetails.getId().toString()),
                eq(IGNORE_WARNING),
                any()
            );
    }

    @Test
    public void shouldStoreCitizenClaimIntoCCD() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimantBuilder().build();

        CaseDetails prepaymentCaseDetails = successfulPrePaymentCoreCaseDataStoreSubmitResponse();
        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            anyMap()
            )
        ).willReturn(ImmutableList.of(prepaymentCaseDetails))
            .willReturn(ImmutableList.of(successfulCoreCaseDataStoreSubmitResponse()));

        given(coreCaseDataApi.startEventForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(prepaymentCaseDetails.getId().toString()),
            eq(SUBMIT_POST_PAYMENT)
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitEventForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(prepaymentCaseDetails.getId().toString()),
            eq(IGNORE_WARNING),
            any()
            )
        ).willReturn(successfulCoreCaseDataStoreSubmitResponse());

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        makeIssueClaimRequest(claimData, AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(coreCaseDataApi)
            .startEventForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(prepaymentCaseDetails.getId().toString()),
                eq(SUBMIT_POST_PAYMENT)
            );

        verify(coreCaseDataApi)
            .submitEventForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(prepaymentCaseDetails.getId().toString()),
                eq(IGNORE_WARNING),
                any()
            );
    }


    @Test
    public void shouldFailIssuingClaimEvenWhenCCDStoreFailsToStartEvent() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();

        CaseDetails prepaymentCaseDetails = successfulPrePaymentCoreCaseDataStoreSubmitResponse();
        given(coreCaseDataApi.searchForCaseworker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            anyMap()
            )
        ).willReturn(ImmutableList.of(prepaymentCaseDetails));

        given(coreCaseDataApi.startEventForCaseWorker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(prepaymentCaseDetails.getId().toString()),
            eq(SUBMIT_POST_PAYMENT)
            )
        ).willThrow(FeignException.class);

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        MvcResult result = makeIssueClaimRequest(claimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isInternalServerError())
            .andReturn();

        assertThat(result.getResolvedException().getMessage())
            .isEqualTo("Failed updating claim in CCD store for claim 000LR003 on event SUBMIT_POST_PAYMENT");
    }

    @Test
    public void shouldIssueClaimEvenWhenCCDStoreFailsToSubmitEvent() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();

        CaseDetails prepaymentCaseDetails = successfulPrePaymentCoreCaseDataStoreSubmitResponse();
        given(coreCaseDataApi.searchForCaseworker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            anyMap()
            )
        ).willReturn(ImmutableList.of(prepaymentCaseDetails));

        given(coreCaseDataApi.startEventForCaseWorker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(prepaymentCaseDetails.getId().toString()),
            eq(SUBMIT_CLAIM_EVENT)
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitEventForCaseWorker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(prepaymentCaseDetails.getId().toString()),
            eq(IGNORE_WARNING),
            any()
            )
        ).willThrow(FeignException.class);

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        MvcResult result = makeIssueClaimRequest(claimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isInternalServerError())
            .andReturn();

        assertThat(result.getResolvedException().getMessage())
            .isEqualTo("Failed updating claim in CCD store for claim 000LR001 on event SUBMIT_POST_PAYMENT");
    }

    @Test
    public void shouldIssueClaimEvenWhenS2STokenGenerationFails() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();

        given(serviceAuthorisationApi.serviceToken(anyMap())).willThrow(FeignException.class);

        MvcResult result = makeIssueClaimRequest(claimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isNotFound())
            .andReturn();

        assertThat(result.getResolvedException().getMessage())
            .isEqualTo(String.format("Case %s not found.", claimData.getExternalId().toString()));

    }
}
