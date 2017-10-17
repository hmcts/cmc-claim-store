package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefendantLinkStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IsDefendantLinkedTest extends BaseIntegrationTest {

    @Test
    public void shouldReturn200HttpStatusAndStatusTrueWhenClaimFoundAndIsLinked() throws Exception {
        Claim claim = claimStore.save(SampleClaimData.builder().build());

        claimRepository.linkDefendant(claim.getId(), 1L);

        MvcResult result = webClient
            .perform(get("/claims/" + claim.getReferenceNumber() + "/defendant-link-status"))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, DefendantLinkStatus.class))
            .isEqualTo(new DefendantLinkStatus(true));
    }

    @Test
    public void shouldReturn200HttpStatusAndStatusFalseWhenClaimFoundAndIsNotLinked() throws Exception {
        Claim claim = claimStore.save(SampleClaimData.builder().build());

        MvcResult result = webClient
            .perform(get("/claims/" + claim.getReferenceNumber() + "/defendant-link-status"))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, DefendantLinkStatus.class))
            .isEqualTo(new DefendantLinkStatus(false));
    }

    @Test
    public void shouldReturn200HttpStatusAndStatusFalseWhenNotClaimFound() throws Exception {
        MvcResult result = webClient
            .perform(get("/claims/000MC900/defendant-link-status"))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, DefendantLinkStatus.class))
            .isEqualTo(new DefendantLinkStatus(false));
    }
}
