package uk.gov.hmcts.cmc.claimstore.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.SUBMITTER_EMAIL;

public class GetClamsByClaimantIdTest extends BaseTest {

    @Test
    public void shouldReturn200HttpStatusAndClaimListWhenClaimsExist() throws Exception {
        long claimantId = 1L;

        given(claimRepository.getBySubmitterId(claimantId))
            .willReturn(Lists.newArrayList(newClaim(1L, claimantId)));

        MvcResult result = webClient
            .perform(get("/claims/claimant/" + claimantId))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserialize(result)).containsExactly(newClaim(1L, claimantId));
    }

    @Test
    public void shouldReturn200HttpStatusAndEmptyClaimListWhenClaimsDoNotExist() throws Exception {
        long claimantId = 1L;

        given(claimRepository.getBySubmitterId(claimantId))
            .willReturn(Lists.newArrayList());

        MvcResult result = webClient
            .perform(get("/claims/claimant/" + claimantId))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserialize(result)).isEmpty();
    }

    @Test
    public void shouldReturn404HttpStatusWhenClaimantIdParameterIsNotNumber() throws Exception {
        webClient
            .perform(get("/claims/claimant/not-a-number"))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        long claimantId = 1L;

        given(claimRepository.getBySubmitterId(claimantId))
            .willThrow(new UnableToExecuteStatementException("Unexpected error", (StatementContext) null));

        webClient
            .perform(get("/claims/claimant/" + claimantId))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

    private Claim newClaim(Long id, Long claimantId) {
        return new Claim(id, claimantId, 3L, 1L, "external-id",
            "000MC001", null,null, null, null,
            false, SUBMITTER_EMAIL, null, null, null);
    }

    private List<Claim> deserialize(MvcResult result) throws UnsupportedEncodingException {
        return jsonMapper.fromJson(result.getResponse().getContentAsString(), new TypeReference<List<Claim>>() {
        });
    }

}
