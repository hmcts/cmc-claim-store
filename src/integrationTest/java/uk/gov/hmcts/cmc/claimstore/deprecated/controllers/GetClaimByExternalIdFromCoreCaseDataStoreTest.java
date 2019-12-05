package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseGetTest;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.listOfCaseDetails;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api"
    }
)
@Ignore // Ignored until we decide how we are testing against CCD
public class GetClaimByExternalIdFromCoreCaseDataStoreTest extends BaseGetTest {
    private static final String SERVICE_TOKEN = "S2S token";

    @Before
    public void before() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void shouldFindClaimFromCCD() throws Exception {
        String externalId = "9c9a87c3-1ec1-4d9d-aa7c-a500558b667b";

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.externalId", externalId))
            )
        ).willReturn(listOfCaseDetails());

        makeRequest("/claims/" + externalId)
            .andExpect(status().isOk())
            .andReturn();

        verify(coreCaseDataApi)
            .searchForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(ImmutableMap.of("case.externalId", externalId.toString()))
            );
    }

    @Test
    public void shouldSearchCCDEvenWhenNoClaimFound() throws Exception {
        String nonExistingExternalId = "efa77f92-6fb6-45d6-8620-8662176786f1";

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.externalId", nonExistingExternalId))
            )
        ).willReturn(Collections.emptyList());

        makeRequest("/claims/" + nonExistingExternalId)
            .andExpect(status().isNotFound());

        verify(coreCaseDataApi)
            .searchForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(ImmutableMap.of("case.externalId", nonExistingExternalId))
            );
    }
}
