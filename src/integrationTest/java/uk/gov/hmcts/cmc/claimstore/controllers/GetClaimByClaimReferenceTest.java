package uk.gov.hmcts.cmc.claimstore.controllers;

import org.assertj.core.util.Maps;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.HashMap;
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

    private static final String AUTH_TOKEN = "I am a valid token";

    private static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withUserId("1")
        .withMail("submitter@example.com")
        .build();

    private static final Map<String, String> HEADERS = Maps.newHashMap(HttpHeaders.AUTHORIZATION, AUTH_TOKEN);

    @Test
    public void shouldReturn200HttpStatusWhenClaimFound() throws Exception {
        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        MvcResult result = makeRequest(claim.getReferenceNumber(), HEADERS)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getReferenceNumber).containsExactly(claim.getReferenceNumber());
    }

    @Test
    public void shouldReturn404HttpStatusWhenNoClaimFound() throws Exception {

        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(USER_DETAILS);
        String nonExistingReferenceNumber = "999LR999";

        makeRequest(nonExistingReferenceNumber, HEADERS)
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn400HttpStatusWhenNoAuthorizationHeaderSet() throws Exception {

        makeRequest("000LR001", new HashMap<>())
            .andExpect(status().isBadRequest());
    }

    private ResultActions makeRequest(String referenceNumber, Map<String, String> headers) throws Exception {
        MockHttpServletRequestBuilder builder = get("/claims/" + referenceNumber);

        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }

        return webClient.perform(builder);
    }
}
