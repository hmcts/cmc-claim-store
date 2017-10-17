package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LinkDefendantToClaimTest extends BaseIntegrationTest {

    @Test
    public void shouldReturn200HttpStatusAndUpdatedClaimWhenLinkIsSuccessfullySet() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        MvcResult result = makeRequest(claim.getId(), 1)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getId, Claim::getDefendantId)
            .containsExactly(claim.getId(), 1L);
    }

    @Test
    public void shouldReturn404HttpStatusWhenClaimDoesNotExist() throws Exception {
        long nonExistingClaimId = 900;

        makeRequest(nonExistingClaimId, 1)
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn404HttpStatusWhenClaimParameterIsNotNumber() throws Exception {
        makeRequest("not-a-number", 1)
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn404HttpStatusWhenDefendantParameterIsNotNumber() throws Exception {
        makeRequest(1, "not-a-number")
            .andExpect(status().isNotFound());
    }

    private ResultActions makeRequest(Object claimId, Object defendantId) throws Exception {
        return webClient
            .perform(put("/claims/" + claimId + "/defendant/" + defendantId));
    }
}
