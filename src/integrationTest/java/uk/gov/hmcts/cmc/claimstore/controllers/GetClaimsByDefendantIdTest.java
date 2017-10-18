package uk.gov.hmcts.cmc.claimstore.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimsByDefendantIdTest extends BaseTest {

    @Test
    public void shouldReturn200HttpStatusAndClaimListWhenClaimsExist() throws Exception {
        long defendantId = 1L;

        given(claimRepository.getByDefendantId(defendantId))
            .willReturn(Lists.newArrayList(newClaim(1L, defendantId)));

        MvcResult result = webClient
            .perform(get("/claims/defendant/" + defendantId))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserialize(result)).containsExactly(newClaim(1L, defendantId));
    }

    @Test
    public void shouldReturn200HttpStatusAndEmptyClaimListWhenClaimsDoNotExist() throws Exception {
        long defendantId = 1L;

        given(claimRepository.getByDefendantId(defendantId))
            .willReturn(Lists.newArrayList());

        MvcResult result = webClient
            .perform(get("/claims/defendant/" + defendantId))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserialize(result)).isEmpty();
    }

    @Test
    public void shouldReturn404HttpStatusWhenDefendantParameterIsNotNumber() throws Exception {
        webClient
            .perform(get("/claims/defendant/not-a-number"))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        long defendantId = 1L;

        given(claimRepository.getByDefendantId(defendantId))
            .willThrow(new UnableToExecuteStatementException("Unexpected error", (StatementContext) null));

        webClient
            .perform(get("/claims/defendant/" + defendantId))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

    private Claim newClaim(Long id, String defendantId) {
        return SampleClaim.builder().withClaimId(id).withDefendantId(defendantId).build();
    }

    private List<Claim> deserialize(MvcResult result) throws UnsupportedEncodingException {
        return jsonMapper.fromJson(result.getResponse().getContentAsString(), new TypeReference<List<Claim>>() {
        });
    }

}
