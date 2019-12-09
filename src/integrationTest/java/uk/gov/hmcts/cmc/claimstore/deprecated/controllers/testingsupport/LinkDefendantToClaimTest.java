package uk.gov.hmcts.cmc.claimstore.deprecated.controllers.testingsupport;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseIntegrationTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "claim-store.test-support.enabled=true",
        "core_case_data.api.url=false",
    }
)
public class LinkDefendantToClaimTest extends BaseIntegrationTest {

    @Test
    public void shouldReturn404HttpStatusWhenClaimDoesNotExist() throws Exception {
        performClaimLinking("not-existing-claim-number")
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn200HttpStatusWhenLinkIsSuccessfullySet() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        performClaimLinking(claim.getReferenceNumber())
            .andExpect(status().isOk());

        assertThat(claimStore.getClaim(claim.getId()))
            .extracting(Claim::getId, Claim::getDefendantId)
            .containsExactly(claim.getId(), "555");
    }

    private ResultActions performClaimLinking(String claimReferenceNumber) throws Exception {
        return webClient
            .perform(put("/testing-support/claims/" + claimReferenceNumber + "/defendant/555"));
    }
}
