package uk.gov.hmcts.cmc.claimstore.controllers.support;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SupportS2SValidationTest extends BaseMockSpringTest {

    @Test
    public void shouldReturn403WhenServiceAuthorizationHeaderIsMissingForSupportClaim() throws Exception {
        webClient.perform(put("/support/claim/000MC001/event/claim/resend-staff-notifications")
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isForbidden());
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
