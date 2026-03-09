package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimsS2SValidationTest extends BaseMockSpringTest {

    @Test
    public void shouldReturn403WhenServiceAuthorizationHeaderIsMissingForDefendantLinkStatus() throws Exception {
        webClient.perform(get("/claims/{caseReference}/defendant-link-status", SampleClaim.REFERENCE_NUMBER)
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn403WhenServiceAuthorizationHeaderIsMissingForClaimMetadata() throws Exception {
        webClient.perform(get("/claims/{externalId}/metadata", SampleClaim.EXTERNAL_ID)
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn403WhenServiceAuthorizationHeaderIsMissingForClaimLetter() throws Exception {
        webClient.perform(get("/claims/letter/{letterHolderId}", SampleClaim.LETTER_HOLDER_ID)
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isForbidden());
    }
}
