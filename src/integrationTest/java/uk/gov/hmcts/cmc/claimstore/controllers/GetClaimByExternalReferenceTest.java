package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimByExternalReferenceTest extends BaseIntegrationTest {

    @Test
    public void shouldReturn404HttpStatusWhenNoClaimFound() throws Exception {

        String nonExistingExternalReferenceNumber = "Ref99999";

        makeRequest(nonExistingExternalReferenceNumber)
            .andExpect(status().isNotFound());
    }

    private ResultActions makeRequest(String externalReferenceNumber) throws Exception {
        return webClient
            .perform(get("/representative/" + externalReferenceNumber));
    }
}
