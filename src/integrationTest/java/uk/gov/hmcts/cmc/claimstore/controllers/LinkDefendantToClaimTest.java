package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LinkDefendantToClaimTest extends BaseTest {

    @Test
    public void shouldReturn200HttpStatusAndUpdatedClaimWhenLinkIsSuccessfullySet() throws Exception {
        Claim claim = claimStore.save(SampleClaimData.builder().build());

        MvcResult result = webClient
            .perform(put("/claims/" + claim.getId() + "/defendant/" + 1))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getId, Claim::getDefendantId)
            .containsExactly(claim.getId(), 1L);
    }

    @Test
    public void shouldReturn404HttpStatusWhenClaimDoesNotExist() throws Exception {
        long nonExistingClaimId = 900L;

        webClient
            .perform(put("/claims/" + nonExistingClaimId + "/defendant/2"))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturn404HttpStatusWhenClaimParameterIsNotNumber() throws Exception {
        webClient
            .perform(put("/claims/not-a-number/defendant/2"))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturn404HttpStatusWhenDefendantParameterIsNotNumber() throws Exception {
        webClient
            .perform(put("/claims/1/defendant/not-a-number"))
            .andExpect(status().isNotFound())
            .andReturn();
    }
}
