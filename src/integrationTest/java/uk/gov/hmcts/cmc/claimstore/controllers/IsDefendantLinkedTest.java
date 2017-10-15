package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Ignore;
import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.models.DefendantLinkStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
public class IsDefendantLinkedTest extends BaseTest {

    private static final String CLAIM_REFERENCE_NUMBER = "000MC001";
    private static final String ENDPOINT = "/claims/" + CLAIM_REFERENCE_NUMBER + "/defendant-link-status";

    @Test
    public void shouldReturn200HttpStatusAndStatusTrueWhenClaimFoundAndIsLinked() throws Exception {

        given(claimRepository.getByClaimReferenceNumber(CLAIM_REFERENCE_NUMBER))
            .willReturn(Optional.of(SampleClaim.builder().build()));

        MvcResult result = webClient
            .perform(get(ENDPOINT))
            .andExpect(status().isOk())
            .andReturn();

        final DefendantLinkStatus status = jsonMapper.fromJson(result.getResponse().getContentAsString(),
            DefendantLinkStatus.class);

        assertThat(status).isEqualTo(new DefendantLinkStatus(true));
    }

    @Test
    public void shouldReturn200HttpStatusAndStatusFalseWhenClaimFoundAndIsNotLinked() throws Exception {

        given(claimRepository.getByClaimReferenceNumber(CLAIM_REFERENCE_NUMBER))
            .willReturn(Optional.of(SampleClaim.builder().withDefendantId(null).build()));

        MvcResult result = webClient
            .perform(get(ENDPOINT))
            .andExpect(status().isOk())
            .andReturn();

        final DefendantLinkStatus status = jsonMapper.fromJson(result.getResponse().getContentAsString(),
            DefendantLinkStatus.class);

        assertThat(status).isEqualTo(new DefendantLinkStatus(false));
    }

    @Test
    public void shouldReturn200HttpStatusAndStatusFalseWhenNotClaimFound() throws Exception {

        given(claimRepository.getByClaimReferenceNumber(eq(CLAIM_REFERENCE_NUMBER))).willReturn(Optional.empty());

        MvcResult result = webClient
            .perform(get(ENDPOINT))
            .andExpect(status().isOk())
            .andReturn();

        final DefendantLinkStatus status = jsonMapper.fromJson(result.getResponse().getContentAsString(),
            DefendantLinkStatus.class);

        assertThat(status).isEqualTo(new DefendantLinkStatus(false));
    }

    @Test
    public void shouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {

        given(claimRepository.getByClaimReferenceNumber(CLAIM_REFERENCE_NUMBER))
            .willThrow(new UnableToExecuteStatementException("Unexpected error", (StatementContext) null));

        webClient
            .perform(get(ENDPOINT))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

}
