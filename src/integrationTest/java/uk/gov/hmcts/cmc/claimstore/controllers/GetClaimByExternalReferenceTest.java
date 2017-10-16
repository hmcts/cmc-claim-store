package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimByExternalReferenceTest extends BaseTest {

    private static final String EXTERNAL_REFERENCE = "Ref123";

    private final Claim claim = new Claim.Builder().setId(1L).setSubmitterId(2L)
        .setReferenceNumber(EXTERNAL_REFERENCE)
        .build();

    @Test
    public void shouldReturn200HttpStatusWhenClaimFound() throws Exception {

        given(claimRepository.getByExternalReference(eq(EXTERNAL_REFERENCE))).willReturn(Collections.singletonList(claim));

        webClient
            .perform(get("/claims/representative/" + EXTERNAL_REFERENCE))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldReturn500HttpStatusWhenInternalErrorOccurs() throws Exception {
        given(claimRepository.getByExternalReference(eq(EXTERNAL_REFERENCE))).willThrow(new RuntimeException("error"));

        webClient
            .perform(get("/claims/representative/" + EXTERNAL_REFERENCE))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }
}
