package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimsByDefendantIdTest extends BaseIntegrationTest {

    @Test
    public void shouldReturn200HttpStatusAndClaimListWhenClaimsExist() throws Exception {
        long defendantId = 1L;

        Claim claim = claimStore.save(SampleClaimData.builder().build());
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
        long nonExistingDefendantId = 900L;

        MvcResult result = makeRequest(nonExistingDefendantId)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .isEmpty();
    }

    @Test
    public void shouldReturn404HttpStatusWhenDefendantParameterIsNotNumber() throws Exception {
        makeRequest("not-a-number")
            .andExpect(status().isNotFound());
    }

    private ResultActions makeRequest(Object defendantId) throws Exception {
        return webClient
            .perform(get("/claims/defendant/" + defendantId));
    }
}
