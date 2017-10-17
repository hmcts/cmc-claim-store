package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimByExternalReferenceTest extends BaseTest {

    private static final String EXTERNAL_REFERENCE = "Ref123";

    private final Claim claim = new Claim.Builder().setId(1L).setSubmitterId(SUBMITTER_ID)
        .setReferenceNumber(EXTERNAL_REFERENCE)
        .build();

    @Before
    public void setup() {
        final UserDetails userDetails
            = SampleUserDetails.builder().withUserId(SUBMITTER_ID).withMail("claimant@email.com").build();

        given(userService.getUserDetails(anyString())).willReturn(userDetails);
    }

    @Test
    public void shouldReturn200HttpStatusWhenClaimFound() throws Exception {

        given(claimRepository.getByExternalReference(eq(EXTERNAL_REFERENCE), eq(SUBMITTER_ID)))
            .willReturn(Collections.singletonList(claim));

        webClient
            .perform(get("/claims/representative/" + EXTERNAL_REFERENCE)
                .header(HttpHeaders.AUTHORIZATION, "token"))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldReturn500HttpStatusWhenInternalErrorOccurs() throws Exception {
        given(claimRepository.getByExternalReference(eq(EXTERNAL_REFERENCE), eq(SUBMITTER_ID))).willThrow(new RuntimeException("error"));

        webClient
            .perform(get("/claims/representative/" + EXTERNAL_REFERENCE)
                .header(HttpHeaders.AUTHORIZATION, "token"))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }
}
