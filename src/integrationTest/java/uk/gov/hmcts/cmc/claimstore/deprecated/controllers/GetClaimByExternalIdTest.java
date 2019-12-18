package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseGetTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class GetClaimByExternalIdTest extends BaseGetTest {
    @Test
    public void shouldReturn200HttpStatusWhenClaimFound() throws Exception {
        UUID externalId = UUID.randomUUID();

        claimStore.saveClaim(SampleClaimData.builder().withExternalId(externalId).build());

        MvcResult result = makeRequest("/claims/" + externalId.toString())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getExternalId).isEqualTo(externalId.toString());
    }

    @Test
    public void shouldReturn404HttpStatusWhenNoClaimFound() throws Exception {
        String nonExistingExternalId = "efa77f92-6fb6-45d6-8620-8662176786f1";

        makeRequest("/claims/" + nonExistingExternalId)
            .andExpect(status().isNotFound());
    }
}
