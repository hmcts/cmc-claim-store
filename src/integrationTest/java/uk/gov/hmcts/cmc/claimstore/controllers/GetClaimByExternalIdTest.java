package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimByExternalIdTest extends BaseTest {

    @Test
    public void shouldReturn200HttpStatusWhenClaimFound() throws Exception {
        UUID externalId = UUID.randomUUID();

        claimStore.save(SampleClaimData.builder().withExternalId(externalId).build());

        MvcResult result = webClient
            .perform(get("/claims/" + externalId))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result))
            .extracting(Claim::getExternalId).containsExactly(externalId.toString());
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
        String nonExistingExternalId = "efa77f92-6fb6-45d6-8620-8662176786f1";

        webClient
            .perform(get("/claims/" + nonExistingExternalId))
            .andExpect(status().isNotFound())
            .andReturn();
    }
}
