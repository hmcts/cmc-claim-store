package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.DefendantLinkStatus;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IsDefendantLinkedTest extends BaseIntegrationTest {
    private static final String AUTHORISATION_TOKEN = "Bearer token";

    @Test
    public void shouldReturn200HttpStatusAndStatusTrueWhenClaimFoundAndIsLinked() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        claimRepository.linkDefendant(claim.getId(), "1");

        MvcResult result = makeRequest(claim.getReferenceNumber())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, DefendantLinkStatus.class))
            .isEqualTo(new DefendantLinkStatus(true));
    }

    @Test
    public void shouldReturn200HttpStatusAndStatusFalseWhenClaimFoundAndIsNotLinked() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        MvcResult result = makeRequest(claim.getReferenceNumber())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, DefendantLinkStatus.class))
            .isEqualTo(new DefendantLinkStatus(false));
    }

    @Test
    public void shouldReturn200HttpStatusAndStatusFalseWhenNotClaimFound() throws Exception {
        String nonExistingReferenceNumber = "000MC900";

        MvcResult result = makeRequest(nonExistingReferenceNumber)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, DefendantLinkStatus.class))
            .isEqualTo(new DefendantLinkStatus(false));
    }

    private ResultActions makeRequest(String referenceNumber) throws Exception {
        return webClient
            .perform(get("/claims/" + referenceNumber + "/defendant-link-status")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
            );
    }
}
