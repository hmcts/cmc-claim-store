package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GetClaimByClaimReferenceTest extends BaseTest {

    private static final String REFERENCE_NUMBER = "000LR001";
    private static final String EXTERNAL_ID = "9f49d8df-b734-4e86-aeb6-e22f0c2ca78d";

    private final Claim claim = new Claim.Builder().setId(1L).setSubmitterId(SUBMITTER_ID)
        .setExternalId(EXTERNAL_ID)
        .setReferenceNumber(REFERENCE_NUMBER)
        .build();

    @Before
    public void setup() {
        final UserDetails userDetails
            = SampleUserDetails.builder().withUserId(SUBMITTER_ID).withMail("claimant@email.com").build();

        given(userService.getUserDetails(anyString())).willReturn(userDetails);
    }

    @Test
    public void shouldReturn200HttpStatusWhenClaimFound() throws Exception {

        given(claimRepository.getByClaimReferenceAndSubmitter(eq(REFERENCE_NUMBER), eq(SUBMITTER_ID))).willReturn(Optional.of(claim));

        webClient
            .perform(get("/claims/" + REFERENCE_NUMBER)
                .header(HttpHeaders.AUTHORIZATION, "token"))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldReturn404HttpStatusWhenNoClaimFound() throws Exception {

        given(claimRepository.getByClaimReferenceNumber(eq(REFERENCE_NUMBER))).willReturn(Optional.empty());

        webClient
            .perform(get("/claims/" + REFERENCE_NUMBER)
                .header(HttpHeaders.AUTHORIZATION, "token"))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturn404HttpStatusWhenWrongUrlGiven() throws Exception {

        webClient
            .perform(get("/claims/wrong-url-path")
                .header(HttpHeaders.AUTHORIZATION, "token"))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturn500HttpStatusWhenInternalErrorOccurs() throws Exception {
        given(claimRepository.getByClaimReferenceAndSubmitter(eq(REFERENCE_NUMBER), eq(SUBMITTER_ID))).willThrow(new RuntimeException("error"));

        webClient
            .perform(get("/claims/" + REFERENCE_NUMBER)
                .header(HttpHeaders.AUTHORIZATION, "token"))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }
}
