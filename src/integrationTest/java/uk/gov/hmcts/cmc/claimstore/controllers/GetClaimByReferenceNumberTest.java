package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
public class GetClaimByReferenceNumberTest extends BaseTest {

    private static final String REFERENCE_NUMBER = "000MC001";
    private static final String EXTERNAL_ID = "9f49d8df-b734-4e86-aeb6-e22f0c2ca78d";

    private final Claim claim = SampleClaim.builder()
        .withId(1L)
        .withSubmitterId(2L)
        .withLetterHolderId(3L)
        .withExternalId(EXTERNAL_ID)
        .withReferenceNumber(REFERENCE_NUMBER)
        .build();

    @Test
    public void shouldReturn200HttpStatusWhenClaimFound() throws Exception {

        given(claimRepository.getByClaimReferenceNumber(eq(REFERENCE_NUMBER))).willReturn(Optional.of(claim));

        webClient
            .perform(get("/testing-support/claims/" + REFERENCE_NUMBER))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldReturn404HttpStatusWhenNoClaimFound() throws Exception {

        given(claimRepository.getByClaimReferenceNumber(eq(REFERENCE_NUMBER))).willReturn(Optional.empty());

        webClient
            .perform(get("/testing-support/claims/" + REFERENCE_NUMBER))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturn404HttpStatusWhenWrongUrlGiven() throws Exception {

        webClient
            .perform(get("/testing-support/not-existing-endpoint"))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturn500HttpStatusWhenInternalErrorOccurs() throws Exception {
        given(claimRepository.getByClaimReferenceNumber(eq(REFERENCE_NUMBER))).willThrow(new RuntimeException("error"));

        webClient
            .perform(get("/testing-support/claims/" + REFERENCE_NUMBER))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }
}
