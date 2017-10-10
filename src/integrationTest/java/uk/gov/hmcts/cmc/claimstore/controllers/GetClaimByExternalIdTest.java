package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.BaseTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimByExternalIdTest extends BaseTest {

    private static final String EXTERNAL_ID = "067e6162-3b6f-4ae2-a171-2470b63dff00";

    @Test
    public void shouldReturn200HttpStatusWhenClaimFound() throws Exception {
        webClient
            .perform(get("/claims/" + EXTERNAL_ID))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldReturn404HttpStatusWhenExternalIdParamIsNotValid() throws Exception {
        webClient
            .perform(get("/claims/not-a-valid-uuid"))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturn404HttpStatusWhenNoClaimFound() throws Exception {
        webClient
            .perform(get("/claims/" + EXTERNAL_ID))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        webClient
            .perform(get("/claims/" + EXTERNAL_ID))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }
}
