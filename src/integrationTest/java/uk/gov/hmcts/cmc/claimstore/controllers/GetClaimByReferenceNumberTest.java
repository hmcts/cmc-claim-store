package uk.gov.hmcts.cmc.claimstore.controllers;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
public class GetClaimByReferenceNumberTest extends BaseIntegrationTest {

    @Test
    public void shouldReturn200HttpStatusWhenClaimFound() throws Exception {
        Claim claim = claimStore.save(SampleClaimData.builder().build());

        MvcResult result = makeRequest(claim.getReferenceNumber())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getReferenceNumber).containsExactly(claim.getReferenceNumber());
    }

    @Test
    public void shouldReturn404HttpStatusWhenNoClaimFound() throws Exception {
        String nonExistingReferenceNumber = "000MC900";

        makeRequest(nonExistingReferenceNumber)
            .andExpect(status().isNotFound());
    }

    private ResultActions makeRequest(String referenceNumber) throws Exception {
        return webClient
            .perform(get("/testing-support/claims/" + referenceNumber));
    }
}
