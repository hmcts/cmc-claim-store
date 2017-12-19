package uk.gov.hmcts.cmc.claimstore.controllers;

import feign.FeignException;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "feature_toggles.core_case_data=true"
    }
)
public class SaveClaimWithCoreCaseDataStoreTest extends BaseSaveTest {

    private static final String SERVICE_TOKEN = "S2S token";
    private static final String USER_ID = "1";
    private static final String JURISDICTION_ID = "CMC";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";
    private static final String EVENT_ID = "submitClaimEvent";
    private static final boolean IGNORE_WARNING = true;

    @Test
    public void shouldStoreNonRepresentedClaimIntoCCDStore() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();

        given(coreCaseDataApi.start(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN), eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(EVENT_ID)
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submit(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(IGNORE_WARNING),
            any()
            )
        ).willReturn(successfulCoreCaseDataStoreSubmitResponse());

        given(serviceAuthorisationApi.serviceToken(anyString(), anyString())).willReturn(SERVICE_TOKEN);

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);

        verify(coreCaseDataApi)
            .start(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(EVENT_ID)
            );

        verify(coreCaseDataApi)
            .submit(
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
        given(coreCaseDataApi.start(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(EVENT_ID)
            )
        ).willThrow(FeignException.class);

        given(serviceAuthorisationApi.serviceToken(anyString(), anyString())).willReturn(SERVICE_TOKEN);

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
        given(coreCaseDataApi.start(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(EVENT_ID)
            )
        ).willReturn(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.submit(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(IGNORE_WARNING),
            any()
            )
        ).willThrow(FeignException.class);

        given(serviceAuthorisationApi.serviceToken(anyString(), anyString())).willReturn(SERVICE_TOKEN);

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

        given(serviceAuthorisationApi.serviceToken(anyString(), anyString())).willThrow(FeignException.class);

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);
    }
}
