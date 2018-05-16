package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false",
        "core_case_data.api.url=http://core-case-data-api"
    }
)
@Ignore // Ignored until we decide how we are testing against CCD
public class SaveClaimWithCoreCaseDataStoreTest extends BaseSaveTest {

    @Test
    public void shouldStoreRepresentedClaimIntoCCD() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.externalId", claimData.getExternalId().toString()))
            )
        ).willReturn(Collections.emptyList());

        given(coreCaseDataApi.startForCaseworker(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN), eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(EVENT_ID)
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitForCaseworker(
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

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);

        verify(coreCaseDataApi)
            .startForCaseworker(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(EVENT_ID)
            );

        verify(coreCaseDataApi)
            .submitForCaseworker(
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
    public void shouldStoreCitizenClaimIntoCCD() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimantBuilder().build();

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.externalId", claimData.getExternalId().toString()))
            )
        ).willReturn(Collections.emptyList());

        given(coreCaseDataApi.startForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(EVENT_ID)
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

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);

        verify(coreCaseDataApi)
            .startForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(EVENT_ID)
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
    public void shouldIssueClaimEvenWhenCCDStoreFailsToStartEvent() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.externalId", claimData.getExternalId().toString()))
            )
        ).willReturn(Collections.emptyList());

        given(coreCaseDataApi.startForCaseworker(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(EVENT_ID)
            )
        ).willThrow(FeignException.class);

        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);
    }

    @Test
    public void shouldIssueClaimEvenWhenCCDStoreFailsToSubmitEvent() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.externalId", claimData.getExternalId().toString()))
            )
        ).willReturn(Collections.emptyList());

        given(coreCaseDataApi.startForCaseworker(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(EVENT_ID)
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submitForCaseworker(
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

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);
    }

    @Test
    public void shouldIssueClaimEvenWhenS2STokenGenerationFails() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.externalId", claimData.getExternalId().toString()))
            )
        ).willReturn(Collections.emptyList());

        given(serviceAuthorisationApi.serviceToken(anyMap())).willThrow(FeignException.class);

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);
    }
}
