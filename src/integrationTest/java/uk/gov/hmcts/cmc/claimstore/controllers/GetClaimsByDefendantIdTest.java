package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseGetTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimsByDefendantIdTest extends BaseGetTest {
    @Test
    public void shouldReturn200HttpStatusAndClaimListWhenClaimsExist() throws Exception {
        String defendantId = "1";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        caseRepository.linkDefendant(claim.getExternalId(), defendantId, AUTHORISATION_TOKEN);

        MvcResult result = makeRequest("/claims/defendant/" + defendantId)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .hasSize(1).first()
            .extracting(Claim::getDefendantId).containsExactly(defendantId);
    }

    @Test
    public void shouldReturn200HttpStatusAndEmptyClaimListWhenClaimsDoNotExist() throws Exception {
        String nonExistingDefendantId = "900";

        MvcResult result = makeRequest("/claims/defendant/" + nonExistingDefendantId)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .isEmpty();
    }
}
