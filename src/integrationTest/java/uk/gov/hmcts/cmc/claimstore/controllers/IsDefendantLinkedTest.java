package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.DefendantLinkStatus;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class IsDefendantLinkedTest extends BaseIntegrationTest {


    @Test
    public void shouldReturn200HttpStatusAndStatusTrueWhenClaimFoundAndIsLinkedV1() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        caseRepository.linkDefendantV1(claim.getExternalId(), "1", BEARER_TOKEN);

        MvcResult result = makeRequest(claim.getReferenceNumber())
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, DefendantLinkStatus.class))
            .isEqualTo(new DefendantLinkStatus(true));
    }

    @Test
    public void shouldReturn200HttpStatusAndStatusTrueWhenClaimFoundAndIsLinkedV2() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        caseRepository.linkDefendantV2(BEARER_TOKEN);

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
            .perform(get("/claims/" + referenceNumber + "/defendant-link-status"));
    }
}
