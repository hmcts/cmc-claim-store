package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimByClaimReferenceTest extends BaseIntegrationTest {

    @Test
    public void shouldReturn404HttpStatusWhenNoClaimFound() throws Exception {

        String nonExistingReferenceNumber = "Ref1232";

        makeRequest(nonExistingReferenceNumber)
            .andExpect(status().isNotFound());
    }

    private ResultActions makeRequest(String referenceNumber) throws Exception {
        return webClient
            .perform(get("/claims/" + referenceNumber));
    }
}
