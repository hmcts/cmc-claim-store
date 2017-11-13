package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleClaimData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimsByDefendantIdTest extends BaseIntegrationTest {

    @Test
    public void shouldReturn200HttpStatusAndClaimListWhenClaimsExist() throws Exception {
        String defendantId = "1";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        claimRepository.linkDefendant(claim.getId(), defendantId);

        MvcResult result = makeRequest(defendantId)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .hasSize(1).first()
            .extracting(Claim::getDefendantId).containsExactly(defendantId);
    }

    @Test
    public void shouldReturn200HttpStatusAndEmptyClaimListWhenClaimsDoNotExist() throws Exception {
        String nonExistingDefendantId = "900";

        MvcResult result = makeRequest(nonExistingDefendantId)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .isEmpty();
    }

    private ResultActions makeRequest(String defendantId) throws Exception {
        return webClient
            .perform(get("/claims/defendant/" + defendantId));
    }
}
