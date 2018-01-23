package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimByExternalIdTest extends BaseIntegrationTest {
    private static final String AUTHORISATION_TOKEN = "Bearer token";

    @Test
    public void shouldReturn200HttpStatusWhenClaimFound() throws Exception {
        UUID externalId = UUID.randomUUID();

        claimStore.saveClaim(SampleClaimData.builder().withExternalId(externalId).build());

        MvcResult result = makeRequest(externalId.toString())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getExternalId).containsExactly(externalId.toString());
    }

    @Test
    public void shouldReturn404HttpStatusWhenNoClaimFound() throws Exception {
        String nonExistingExternalId = "efa77f92-6fb6-45d6-8620-8662176786f1";

        makeRequest(nonExistingExternalId)
            .andExpect(status().isNotFound());
    }

    private ResultActions makeRequest(String externalId) throws Exception {
        return webClient
            .perform(get("/claims/" + externalId)
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
            );
    }
}
