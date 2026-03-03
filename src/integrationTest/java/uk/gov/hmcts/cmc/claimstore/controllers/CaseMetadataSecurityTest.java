package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserInfo;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CaseMetadataSecurityTest extends BaseMockSpringTest {

    @MockBean
    private ClaimService claimService;

    private static final String S2S_TOKEN = "Bearer s2s-token";

    @BeforeEach
    public void setup() {
        when(claimService.getClaimsByState(any(), any())).thenReturn(Collections.emptyList());
        UserInfo userInfo = UserInfo.builder()
            .uid(USER_ID)
            .roles(Arrays.asList("citizen"))
            .build();
        when(userService.getUserInfo(anyString())).thenReturn(userInfo);
    }

    @Test
    public void shouldReturn401WhenServiceAuthorizationHeaderIsMissing() throws Exception {
        webClient.perform(get("/claims/filters/created")
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturn401WhenServiceAuthorizationTokenIsInvalid() throws Exception {
        when(authTokenValidator.getServiceName(anyString())).thenThrow(new InvalidTokenException("Invalid token"));

        webClient.perform(get("/claims/filters/created")
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
            .header("ServiceAuthorization", "invalid-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturn403WhenServiceIsNotAuthorized() throws Exception {
        when(authTokenValidator.getServiceName(anyString())).thenReturn("unauthorized_service");

        webClient.perform(get("/claims/filters/created")
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
            .header("ServiceAuthorization", S2S_TOKEN))
            .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn200WhenBothTokensAreValidAndServiceIsAuthorized() throws Exception {
        when(authTokenValidator.getServiceName(anyString())).thenReturn("ccd_data");

        webClient.perform(get("/claims/filters/created")
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
            .header("ServiceAuthorization", S2S_TOKEN))
            .andExpect(status().isOk());
    }

    @Test
    public void shouldReturn401WhenAuthorizationHeaderIsMissing() throws Exception {
        // Since ServiceAuthFilter is before UsernamePasswordAuthenticationFilter,
        // it will first check for S2S.
        // If S2S is present but Authorization is missing, then Spring Security will deny access.

        when(authTokenValidator.getServiceName(anyString())).thenReturn("ccd_data");

        webClient.perform(get("/claims/filters/created")
            .header("ServiceAuthorization", S2S_TOKEN))
            .andExpect(status().isUnauthorized());
    }
}
