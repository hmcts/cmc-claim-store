package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import feign.FeignException;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_CASE;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitRepresentativeResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "feature_toggles.async_event_operations_enabled=true"
    }
)
public class SaveClaimWithCoreCaseDataStoreTest extends BaseSaveTest {

    @Test
    public void shouldStoreRepresentedClaimIntoCCD() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();

        given(coreCaseDataApi.startForCaseworker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(CREATE_CASE.getValue())
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

        given(coreCaseDataApi.startEventForCaseWorker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            any(),
            eq(ISSUE_CASE.getValue())
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitEventForCaseWorker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            any(),
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
                eq(CREATE_CASE.getValue())
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

        given(coreCaseDataApi.startForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(CREATE_CASE.getValue())
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

        given(coreCaseDataApi.startEventForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            any(),
            eq(ISSUE_CASE.getValue())
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitEventForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            any(),
            eq(IGNORE_WARNING),
            any()
            )
        ).willReturn(successfulCoreCaseDataStoreSubmitRepresentativeResponse());

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
                eq(CREATE_CASE.getValue())
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

        given(coreCaseDataApi.startForCaseworker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(CREATE_CASE.getValue())
            )
        ).willThrow(FeignException.class);

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        MvcResult result = makeIssueClaimRequest(claimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isInternalServerError())
            .andReturn();

        assertThat(result.getResolvedException().getMessage())
            .isEqualTo("Failed storing claim in CCD store for case id 000LR001 on event CREATE_CASE");
    }

    @Test
    public void shouldIssueClaimEvenWhenCCDStoreFailsToSubmitEvent() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();

        given(coreCaseDataApi.startForCaseworker(
            eq(SOLICITOR_AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(CREATE_CASE.getValue())
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
            .isEqualTo("Failed storing claim in CCD store for case id 000LR001 on event CREATE_CASE");
    }
}
