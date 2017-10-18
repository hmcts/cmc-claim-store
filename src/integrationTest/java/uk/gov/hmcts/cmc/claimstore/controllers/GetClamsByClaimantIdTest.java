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

    private String claimantId = "1";

    @Test
    public void shouldReturn200HttpStatusAndClaimListWhenClaimsExist() throws Exception {
        given(claimRepository.getBySubmitterId(claimantId))
            .willReturn(Lists.newArrayList(newClaim(claimantId)));

        MvcResult result = webClient
            .perform(get("/claims/claimant/" + claimantId))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserialize(result)).containsExactly(newClaim(claimantId));
    }

    @Test
    public void shouldReturn200HttpStatusAndEmptyClaimListWhenClaimsDoNotExist() throws Exception {
        given(claimRepository.getBySubmitterId(claimantId))
            .willReturn(Lists.newArrayList());

        MvcResult result = webClient
            .perform(get("/claims/claimant/" + claimantId))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserialize(result)).isEmpty();
    }

    @Test
    public void shouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {
        String claimantId = "1";

        given(claimRepository.getBySubmitterId(claimantId))
            .willThrow(new UnableToExecuteStatementException("Unexpected error", (StatementContext) null));

        webClient
            .perform(get("/claims/claimant/" + claimantId))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

    private Claim newClaim(String claimantId) {
        return new Claim.Builder().setId(1L).setSubmitterId(claimantId).setLetterHolderId("3").setDefendantId("1")
            .setExternalId("9f49d8df-b734-4e86-aeb6-e22f0c2ca78d").setReferenceNumber("000MC001")
            .setSubmitterEmail(SUBMITTER_EMAIL)
            .build();
    }

    private List<Claim> deserialize(MvcResult result) throws UnsupportedEncodingException {
        return jsonMapper.fromJson(result.getResponse().getContentAsString(), new TypeReference<List<Claim>>() {
        });
    }

}
