package uk.gov.hmcts.cmc.claimstore.controllers;

import org.assertj.core.util.Maps;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmccase.models.Claim;
import uk.gov.hmcts.cmccase.models.sampledata.SampleClaimData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimByExternalReferenceTest extends BaseIntegrationTest {

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
        String externalReferenceNumber = claim.getClaimData()
            .getExternalReferenceNumber()
            .orElseThrow(() -> new RuntimeException("External reference number not present."));
        MvcResult result = makeRequest(externalReferenceNumber, HEADERS)
            .andExpect(status().isOk())
            .andReturn();

        List<Claim> claims = deserializeListFrom(result);
        assertThat(claims.size()).isGreaterThan(0);
        assertThat(claims.get(0).getClaimData().getExternalReferenceNumber().isPresent()).isTrue();
        assertThat(claims.get(0).getClaimData().getExternalReferenceNumber().get()).isEqualTo(externalReferenceNumber);
    }

    @Test
    public void shouldReturn200HttpStatusAndEmptyClaimListWhenClaimsDoNotExist() throws Exception {

        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(USER_DETAILS);
        String nonExistingExternalReferenceNumber = "Ref99999";

        MvcResult result = makeRequest(nonExistingExternalReferenceNumber, HEADERS)
            .andExpect(status().isOk()).andReturn();

        assertThat(deserializeListFrom(result))
            .isEmpty();
    }

    @Test
    public void shouldReturn400HttpStatusWhenNoAuthorizationHeaderSet() throws Exception {

        makeRequest("Ref99999", Collections.emptyMap())
            .andExpect(status().isBadRequest());
    }

    private ResultActions makeRequest(String externalReferenceNumber, Map<String, String> headers) throws Exception {

        MockHttpServletRequestBuilder builder = get("/claims/representative/" + externalReferenceNumber);

        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }

        return webClient.perform(builder);
    }
}
