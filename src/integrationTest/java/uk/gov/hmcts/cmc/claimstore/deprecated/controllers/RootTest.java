package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RootTest extends BaseIntegrationTest {

    @Test
    public void root() throws Exception {
        webClient.perform(get("/"))
            .andExpect(status().isOk());
    }

}
