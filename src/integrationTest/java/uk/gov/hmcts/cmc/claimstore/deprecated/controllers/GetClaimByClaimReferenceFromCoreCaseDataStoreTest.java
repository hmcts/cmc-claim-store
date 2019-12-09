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
public class GetClaimByClaimReferenceFromCoreCaseDataStoreTest extends BaseGetTest {
    private static final String SERVICE_TOKEN = "S2S token";

    @Before
    public void before() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void shouldFindClaimFromCCDForClaimReference() throws Exception {

        final String referenceNumber = "000MC023";

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.previousServiceCaseReference", referenceNumber))
            )
        ).willReturn(listOfCaseDetails());

        makeRequest("/claims/" + referenceNumber)
            .andExpect(status().isOk())
            .andReturn();

        verify(coreCaseDataApi)
            .searchForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(ImmutableMap.of("case.previousServiceCaseReference", referenceNumber))
            );
    }

    @Test
    public void shouldSearchCCDEvenWhenNoClaimFoundInDB() throws Exception {
        String nonExistingReferenceNumber = "999LR999";

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.previousServiceCaseReference", nonExistingReferenceNumber))
            )
        ).willReturn(Collections.emptyList());

        makeRequest("/claims/" + nonExistingReferenceNumber)
            .andExpect(status().isNotFound());

        verify(coreCaseDataApi)
            .searchForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(ImmutableMap.of("case.previousServiceCaseReference", nonExistingReferenceNumber))
            );
    }
}
