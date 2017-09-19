package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_FORENAME;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_SURNAME;

public class SaveCountyCourtJudgmentTest extends BaseTest {

    private static final long CLAIM_ID = 1L;
    private static final long CLAIMANT_ID = 123L;

    @Before
    public void setup() {
        final UserDetails userDetails
            = new UserDetails(CLAIMANT_ID, "claimant@email.com", SUBMITTER_FORENAME, SUBMITTER_SURNAME);

        given(userService.getUserDetails(anyString())).willReturn(userDetails);
    }

    @Test
    public void shouldReturnClaimWithCountyCourtJudgment() throws Exception {

        Claim claimWithCCJ = SampleClaim.builder()
            .withSubmitterId(CLAIMANT_ID)
            .withResponseDeadline(LocalDate.now().minusDays(2))
            .withCountyCourtJudgment(new HashMap<>())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        given(claimRepository.getById(CLAIM_ID))
            .willReturn(Optional.of(
                SampleClaim.builder()
                    .withSubmitterId(CLAIMANT_ID)
                    .withResponseDeadline(LocalDate.now().minusDays(2))
                    .withCountyCourtJudgment(null)
                    .build()
                )
            ).willReturn(Optional.of(claimWithCCJ));

        postCountyCourtJudgment(CLAIM_ID)
            .andExpect(status().isOk());
    }

    private ResultActions postCountyCourtJudgment(final long claimId) throws Exception {
        return webClient
            .perform(post("/claims/" + claimId + "/county-court-judgment")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(new HashMap<>()))
            );
    }
}
