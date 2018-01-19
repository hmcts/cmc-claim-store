package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class GetClaimsByClaimantIdTest extends BaseIntegrationTest {

    @Test
    public void shouldReturn200HttpStatusAndClaimListWhenClaimsExist() throws Exception {
        String submitterId = "1";

        claimStore.saveClaim(SampleClaimData.builder().build(), submitterId, LocalDate.now());

        MvcResult result = makeRequest(submitterId)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .hasSize(1).first()
            .extracting(Claim::getSubmitterId).containsExactly(submitterId);
    }

    @Test
    public void shouldReturn200HttpStatusAndEmptyClaimListWhenClaimsDoNotExist() throws Exception {
        String nonExistingSubmitterId = "900";

        MvcResult result = makeRequest(nonExistingSubmitterId)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeListFrom(result))
            .isEmpty();
    }

    private ResultActions makeRequest(Object submitterId) throws Exception {
        return webClient
            .perform(get("/claims/claimant/" + submitterId)
                .header(HttpHeaders.AUTHORIZATION, "ABC123")
            );
    }
}
