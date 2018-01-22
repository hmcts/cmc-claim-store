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
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.time.LocalDate;
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
public class GetClaimsByClaimantIdFromCoreCaseDataStoreTest extends BaseIntegrationTest {
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
    public void shouldFindClaimFromCCDForClaimantIdHoweverReturnClaimFromPostgres() throws Exception {
        String submitterId = "1";

        claimStore.saveClaim(SampleClaimData.builder().build(), submitterId, LocalDate.now());

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.submitterId", submitterId))
            )
        ).willReturn(successfulCoreCaseDataSearchResponse());

        MvcResult result = makeRequest(submitterId)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .hasSize(1).first()
            .extracting(Claim::getSubmitterId).containsExactly(submitterId);

        verify(coreCaseDataApi)
            .searchForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(ImmutableMap.of("case.submitterId", submitterId))
            );
    }

    @Test
    public void shouldSearchCCDEvenWhenNoClaimFoundInDB() throws Exception {
        String nonExistingSubmitterId = "12";

        given(coreCaseDataApi.searchForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(ImmutableMap.of("case.submitterId", nonExistingSubmitterId))
            )
        ).willReturn(Collections.emptyList());

        MvcResult result = makeRequest(nonExistingSubmitterId)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .isEmpty();

        verify(coreCaseDataApi)
            .searchForCitizen(
                eq(AUTHORISATION_TOKEN),
                eq(SERVICE_TOKEN),
                eq(USER_ID),
                eq(JURISDICTION_ID),
                eq(CASE_TYPE_ID),
                eq(ImmutableMap.of("case.submitterId", nonExistingSubmitterId))
            );
    }

    private ResultActions makeRequest(String externalId) throws Exception {
        return webClient
            .perform(get("/claims/claimant/" + externalId)
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
            );
    }
}
