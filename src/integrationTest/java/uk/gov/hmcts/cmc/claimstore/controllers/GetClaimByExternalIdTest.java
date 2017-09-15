package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.SUBMITTER_EMAIL;

public class GetClaimByExternalIdTest extends BaseTest {

    private static final String EXTERNAL_ID = "067e6162-3b6f-4ae2-a171-2470b63dff00";

    @Test
    public void shouldReturn200HttpStatusWhenClaimFound() throws Exception {

        given(claimRepository.getClaimByExternalId(EXTERNAL_ID))
            .willReturn(Optional.of(newClaim(EXTERNAL_ID)));

        webClient
            .perform(get("/claims/" + EXTERNAL_ID))
            .andExpect(status().isOk())
            .andReturn();
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

        given(claimRepository.getClaimByExternalId(eq(EXTERNAL_ID))).willReturn(Optional.empty());

        webClient
            .perform(get("/claims/" + EXTERNAL_ID))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {

        given(claimRepository.getClaimByExternalId(EXTERNAL_ID))
            .willThrow(new UnableToExecuteStatementException("Unexpected error", (StatementContext) null));

        webClient
            .perform(get("/claims/" + EXTERNAL_ID))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

    private Claim newClaim(String externalId) {
        return new Claim(1L, 2L, 3L, 4L, externalId, "000MC001", null, null, null, null, false, SUBMITTER_EMAIL, null, response, defendantEmail);
    }
}
