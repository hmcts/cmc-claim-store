package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseGetTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimByExternalReferenceTest extends BaseGetTest {

    @Test
    @Ignore("To be fixed as part of task ROC-6278")
    public void shouldReturn200HttpStatusWhenClaimFound() throws Exception {

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        String externalReferenceNumber = claim.getClaimData()
            .getExternalReferenceNumber()
            .orElseThrow(() -> new RuntimeException("External reference number not present."));
        MvcResult result = makeRequest("/claims/representative/" + externalReferenceNumber)
            .andExpect(status().isOk())
            .andReturn();

        List<Claim> claims = deserializeListFrom(result);
        assertThat(claims.size()).isGreaterThan(0);
        assertThat(claims.get(0).getClaimData().getExternalReferenceNumber().isPresent()).isTrue();
        assertThat(claims.get(0).getClaimData().getExternalReferenceNumber().get()).isEqualTo(externalReferenceNumber);
    }

    @Test
    public void shouldReturn200HttpStatusAndEmptyClaimListWhenClaimsDoNotExist() throws Exception {
        String nonExistingExternalReferenceNumber = "Ref99999";

        MvcResult result = makeRequest("/claims/representative/" + nonExistingExternalReferenceNumber)
            .andExpect(status().isOk()).andReturn();

        assertThat(deserializeListFrom(result))
            .isEmpty();
    }

    @Test
    public void shouldReturn400HttpStatusWhenNoAuthorizationHeaderSet() throws Exception {

        webClient.perform(get("/claims/representative/" + "Ref99999"))
            .andExpect(status().isBadRequest());
    }
}
