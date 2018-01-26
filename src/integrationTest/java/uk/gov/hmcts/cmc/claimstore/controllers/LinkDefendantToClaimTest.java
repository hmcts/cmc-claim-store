package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LinkDefendantToClaimTest extends BaseIntegrationTest {

    @Test
    public void shouldReturn200HttpStatusAndUpdatedClaimWhenLinkIsSuccessfullySet() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        MvcResult result = linkdefendantRequest(claim.getExternalId(), "1")
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getId, Claim::getDefendantId)
            .containsExactly(claim.getId(), "1");
    }

    @Test
    public void shouldReturn404HttpStatusWhenClaimDoesNotExist() throws Exception {
        String nonExistingExternalId = "7d293143-b787-454f-aa8e-2fd69a209e52";

        linkdefendantRequest(nonExistingExternalId, "1")
            .andExpect(status().isNotFound());
    }

    private ResultActions linkdefendantRequest(Object externalId, String defendantId) throws Exception {
        return webClient
            .perform(put("/claims/" + externalId + "/defendant/" + defendantId)
            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN));
    }
}
