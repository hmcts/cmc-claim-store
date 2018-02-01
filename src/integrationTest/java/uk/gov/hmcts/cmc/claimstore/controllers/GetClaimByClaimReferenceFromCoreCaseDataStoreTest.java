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
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.Collections;

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
public class GetClaimByClaimReferenceFromCoreCaseDataStoreTest extends BaseGetTest {
    private static final String SERVICE_TOKEN = "S2S token";

    @Before
    public void before() {
        given(jwtHelper.isSolicitor(AUTHORISATION_TOKEN)).willReturn(false);
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void shouldFindClaimFromCCDForClaimReferenceHoweverReturnClaimFromPostgres() throws Exception {

        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();

        final Claim claim = claimStore.saveClaim(claimData);

        final String referenceNumber = claim.getReferenceNumber();

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.referenceNumber", referenceNumber))
            )
        ).willReturn(successfulCoreCaseDataSearchResponse());

        MvcResult result = makeRequest("/claims/" + referenceNumber)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getReferenceNumber).containsExactly(claim.getReferenceNumber());

        verify(coreCaseDataApi)
            .searchForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(ImmutableMap.of("case.referenceNumber", referenceNumber))
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
            eq(ImmutableMap.of("case.referenceNumber", nonExistingReferenceNumber))
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
                eq(ImmutableMap.of("case.referenceNumber", nonExistingReferenceNumber))
            );
    }
}
