package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
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

    @Test
    public void shouldStoreSealedCopyOfNonRepresentedClaimIntoCCDStore() throws Exception {
        final ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();
        ResponseEntity<StartEventResponse> startEventResponseResponseEntity = ResponseEntity
            .ok(successfulCoreCaseDataStoreStartResponse());

        given(coreCaseDataApi.start(
            eq(AUTHORISATION_TOKEN), eq(SERVICE_TOKEN), anyString(), anyString(), anyString(), anyString())
        ).willReturn(startEventResponseResponseEntity);

        ResponseEntity<CaseDetails> caseDetailsResponseEntity = ResponseEntity
            .status(CREATED).body(successfulCoreCaseDataStoreSubmitResponse());

        given(coreCaseDataApi.submit(
            eq(AUTHORISATION_TOKEN), eq(SERVICE_TOKEN), anyString(), anyString(), anyString(), anyBoolean(), any())
        ).willReturn(caseDetailsResponseEntity);

        given(serviceAuthorisationApi.serviceToken(anyString(), anyString())).willReturn(SERVICE_TOKEN);

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);

        verify(coreCaseDataApi)
            .start(eq(AUTHORISATION_TOKEN), eq(SERVICE_TOKEN), anyString(), anyString(), anyString(), anyString());

        verify(coreCaseDataApi)
            .submit(eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                anyString(),
                anyString(),
                anyString(),
                anyBoolean(),
                any());
    }

    @Test
    public void shouldIssueClaimEvenWhenCCDStoreFails() throws Exception {
        final ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();
        given(coreCaseDataApi.start(
            eq(AUTHORISATION_TOKEN), eq(SERVICE_TOKEN), anyString(), anyString(), anyString(), anyString())
        ).willReturn(ResponseEntity.status(INTERNAL_SERVER_ERROR).build());

        given(coreCaseDataApi.submit(
            eq(AUTHORISATION_TOKEN), eq(SERVICE_TOKEN), anyString(), anyString(), anyString(), anyBoolean(), any())
        ).willReturn(ResponseEntity.status(INTERNAL_SERVER_ERROR).build());

        given(serviceAuthorisationApi.serviceToken(anyString(), anyString())).willReturn(SERVICE_TOKEN);

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);
    }

    @Test
    public void shouldIssueClaimEvenWhenInvalidS2SAuthToken() throws Exception {
        final ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();
        given(coreCaseDataApi.start(
            eq(AUTHORISATION_TOKEN), eq("Invalid s2s Token"), anyString(), anyString(), anyString(), anyString())
        ).willReturn(ResponseEntity.status(INTERNAL_SERVER_ERROR).build());

        given(coreCaseDataApi.submit(
            eq(AUTHORISATION_TOKEN), eq(SERVICE_TOKEN), anyString(), anyString(), anyString(), anyBoolean(), any())
        ).willReturn(ResponseEntity.status(INTERNAL_SERVER_ERROR).build());

        given(serviceAuthorisationApi.serviceToken(anyString(), anyString())).willReturn(SERVICE_TOKEN);

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);

    }
}
