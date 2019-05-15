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
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitRepresentativeResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulPrePaymentCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "document_management.url=false",
        "feature_toggles.ccd_async_enabled=false",
        "feature_toggles.ccd_enabled=true",
        "feature_toggles.async_event_operations_enabled=false"
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

        given(coreCaseDataApi.startForCaseworker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(CREATE_CLAIM.getValue())
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitForCaseworker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(IGNORE_WARNING),
            any()
            )
        ).willReturn(successfulCoreCaseDataStoreSubmitRepresentativeResponse());

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        makeIssueClaimRequest(claimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(coreCaseDataApi)
            .startForCaseworker(
                eq(SOLICITOR_AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(CREATE_CLAIM.getValue())
            );

        verify(coreCaseDataApi)
            .submitForCaseworker(
                eq(SOLICITOR_AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
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

        given(coreCaseDataApi.startForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(CREATE_CLAIM.getValue())
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

        makeIssueClaimRequest(claimData, AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(coreCaseDataApi)
            .startForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(CREATE_CLAIM.getValue())
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

        given(coreCaseDataApi.startForCaseworker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(CREATE_CLAIM.getValue())
            )
        ).willThrow(FeignException.class);

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        MvcResult result = makeIssueClaimRequest(claimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isInternalServerError())
            .andReturn();

        assertThat(result.getResolvedException().getMessage())
            .isEqualTo("Failed storing claim in CCD store for case id 000LR001 on event CREATE_CLAIM");
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

        given(coreCaseDataApi.startForCaseworker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(CREATE_CLAIM.getValue())
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitForCaseworker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(IGNORE_WARNING),
            any()
            )
        ).willThrow(FeignException.class);

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        MvcResult result = makeIssueClaimRequest(claimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isInternalServerError())
            .andReturn();

        assertThat(result.getResolvedException().getMessage())
            .isEqualTo("Failed storing claim in CCD store for case id 000LR001 on event CREATE_CLAIM");
    }
}
