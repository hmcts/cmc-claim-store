package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseGetTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.JURISDICTION_ID;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataSearchResponse;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false"
    }
)
@Ignore
public class GetClaimByExternalIdFromCoreCaseDataStoreTest extends BaseGetTest {
    private static final String SERVICE_TOKEN = "S2S token";

    @Before
    public void before() {
        given(jwtHelper.isSolicitor(AUTHORISATION_TOKEN)).willReturn(false);
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void shouldFindClaimFromCCDHoweverReturnClaimFromPostgres() throws Exception {
        UUID externalId = UUID.randomUUID();

        ClaimData claimData = SampleClaimData.builder().withExternalId(externalId)
            .withAmount(SampleAmountRange.validDefaults())
            .build();

        claimStore.saveClaim(claimData);

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.externalId", externalId.toString()))
            )
        ).willReturn(successfulCoreCaseDataSearchResponse());

        MvcResult result = makeRequest("/claims/" + externalId.toString())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);

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
