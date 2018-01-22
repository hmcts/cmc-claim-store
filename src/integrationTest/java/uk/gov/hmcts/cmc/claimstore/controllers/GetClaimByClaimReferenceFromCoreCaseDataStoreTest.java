package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDClaimRepository.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDClaimRepository.JURISDICTION_ID;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataSearchResponse;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false"
    }
)
public class GetClaimByClaimReferenceFromCoreCaseDataStoreTest extends BaseIntegrationTest {
    private static final String AUTHORISATION_TOKEN = "I am a valid token";
    private static final String SERVICE_TOKEN = "S2S token";
    private static final String USER_ID = "1";

    private static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withUserId(USER_ID)
        .withMail("submitter@example.com")
        .build();

    @Before
    public void before() {
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(USER_DETAILS);
        given(jwtService.isCitizen(AUTHORISATION_TOKEN)).willReturn(true);
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

        MvcResult result = makeRequest(referenceNumber)
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

        makeRequest(nonExistingReferenceNumber)
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

    private ResultActions makeRequest(String referenceNumber) throws Exception {
        return webClient
            .perform(get("/claims/" + referenceNumber)
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
            );
    }
}
