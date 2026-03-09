package uk.gov.hmcts.cmc.claimstore.controllers.support;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.filters.ServiceAuthFilter;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SupportS2SValidationTest extends BaseMockSpringTest {

    @MockBean
    protected ClaimService claimService;

    @Test
    public void shouldReturn403WhenServiceAuthorizationHeaderIsMissingForSupportClaim() throws Exception {
        webClient.perform(put("/support/claim/000MC001/event/claim/resend-staff-notifications")
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn403WhenServiceAuthorizationHeaderIsInvalid() throws Exception {
        when(serviceAuthorisationApi.getServiceName(anyString())).thenThrow(new InvalidTokenException("Invalid token"));

        webClient.perform(put("/support/claim/000MC001/event/claim/resend-staff-notifications")
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
            .header(ServiceAuthFilter.SERVICE_AUTHORIZATION, "invalid-token"))
            .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn403WhenServiceIsNotAuthorized() throws Exception {
        when(serviceAuthorisationApi.getServiceName(anyString())).thenReturn("unauthorized_service");

        webClient.perform(put("/support/claim/000MC001/event/claim/resend-staff-notifications")
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
            .header(ServiceAuthFilter.SERVICE_AUTHORIZATION, "unauthorized-token"))
            .andExpect(status().isForbidden());
    }

    @Test
    public void shouldAllowRequestWhenServiceAuthorizationIsValid() throws Exception {
        when(serviceAuthorisationApi.getServiceName(anyString())).thenReturn("cmc_claim_store");
        User user = new User(BEARER_TOKEN, USER_DETAILS);
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(user);
        when(claimService.getClaimByReferenceAnonymous(anyString())).thenReturn(Optional.empty());

        webClient.perform(put("/support/claim/000MC001/event/claim/resend-staff-notifications")
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
            .header(ServiceAuthFilter.SERVICE_AUTHORIZATION, SERVICE_TOKEN))
            .andExpect(status().isNotFound()); // NotFound is expected because the claim doesn't exist, but it's not 403
    }

    @Test
    public void shouldReturn403WhenServiceAuthorizationHeaderIsMissingForSupportRpa() throws Exception {
        webClient.perform(put("/support/rpa/claim")
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn403WhenServiceAuthorizationHeaderIsMissingForSupportDeadline() throws Exception {
        webClient.perform(put("/support/deadline/dq/claim/000MC001")
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn403WhenServiceAuthorizationHeaderIsMissingForReSendMediation() throws Exception {
        webClient.perform(post("/support/reSendMediation")
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"reportDate\":\"2026-03-09\",\"recipientEmail\":\"test@example.com\"}"))
            .andExpect(status().isForbidden());
    }
}
