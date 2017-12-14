package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.CREATED;
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

        makeRequest(SampleClaimData.submittedByLegalRepresentative())
            .andExpect(status().isOk())
            .andReturn();

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

}
