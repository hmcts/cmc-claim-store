package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClamsByClaimantIdTest extends BaseTest {

    @Test
    public void shouldReturn200HttpStatusAndClaimListWhenClaimsExist() throws Exception {
        long submitterId = 1L;

        claimStore.save(SampleClaimData.builder().build(), submitterId, LocalDate.now());

        MvcResult result = webClient
            .perform(get("/claims/claimant/" + submitterId))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .hasSize(1).first()
            .extracting(Claim::getSubmitterId).containsExactly(submitterId);
    }

    @Test
    public void shouldReturn200HttpStatusAndEmptyClaimListWhenClaimsDoNotExist() throws Exception {
        long nonExistingSubmitterId = 900L;

        MvcResult result = webClient
            .perform(get("/claims/claimant/" + nonExistingSubmitterId))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .isEmpty();
    }

    @Test
    public void shouldReturn404HttpStatusWhenClaimantIdParameterIsNotNumber() throws Exception {
        webClient
            .perform(get("/claims/claimant/not-a-number"))
            .andExpect(status().isNotFound());
    }

}
