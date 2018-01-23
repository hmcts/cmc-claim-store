package uk.gov.hmcts.cmc.claimstore.controllers;

import org.assertj.core.util.Maps;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class GetClaimByClaimReferenceTest extends BaseIntegrationTest {

    private static final String AUTHORISATION_TOKEN = "Bearer token";

    private static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withUserId("1")
        .withMail("submitter@example.com")
        .build();

    private static final Map<String, String> HEADERS = Maps.newHashMap(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN);

    @Test
    public void shouldReturn200HttpStatusWhenClaimFound() throws Exception {
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        MvcResult result = makeRequest("/claims/" + claim.getReferenceNumber())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getReferenceNumber).containsExactly(claim.getReferenceNumber());
    }

    @Test
    public void shouldReturn404HttpStatusWhenNoClaimFound() throws Exception {

        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(USER_DETAILS);
        String nonExistingReferenceNumber = "999LR999";

        makeRequest("/claims/" + nonExistingReferenceNumber)
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn400HttpStatusWhenNoAuthorizationHeaderSet() throws Exception {

        webClient.perform(
            get("/claims/" + "000LR001")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
        ).andExpect(status().isBadRequest());
    }


    private ResultActions makeRequest(String urlTemplate) throws Exception {
        return webClient
            .perform(get(urlTemplate)
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
            );
    }
}
