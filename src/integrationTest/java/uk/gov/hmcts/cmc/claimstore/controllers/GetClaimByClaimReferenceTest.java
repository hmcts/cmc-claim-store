package uk.gov.hmcts.cmc.claimstore.controllers;

import org.assertj.core.util.Maps;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimByClaimReferenceTest extends BaseIntegrationTest {

    private static final String AUTH_TOKEN = "I am a valid token";

    private static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withUserId(SUBMITTER_ID)
        .withMail("submitter@example.com")
        .build();

    @Test
    public void shouldReturn404HttpStatusWhenNoClaimFound() throws Exception {

        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(USER_DETAILS);
        String nonExistingReferenceNumber = "999LR999";

        makeRequest(nonExistingReferenceNumber, Maps.newHashMap(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isNotFound());
    }

    private ResultActions makeRequest(String referenceNumber, Map<String, String> headers) throws Exception {
        MockHttpServletRequestBuilder builder = get("/claims/" + referenceNumber);

        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }

        return webClient.perform(builder);
    }
}
