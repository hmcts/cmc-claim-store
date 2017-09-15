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
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SaveCountyCourtJudgmentTest extends BaseTest {

    private static final long CLAIM_ID = 1L;
    private static final long CLAIMANT_ID = 123L;

    @Before
    public void setup() {

        given(userService.getUserDetails(anyString()))
            .willReturn(new UserDetails(CLAIMANT_ID, "claimant@email.com"));
    }

    @Test
    public void shouldReturnClaimWithCountyCourtJudgment() throws Exception {

        //given
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

        //when
        postCountyCourtJudgment(CLAIM_ID, new HashMap<>())
            .andExpect(status().isOk())
            .andReturn();
    }

    private ResultActions postCountyCourtJudgment(long claimId, Map<String, Object> data) throws Exception {
        return webClient
            .perform(post("/claims/" + claimId + "/county-court-judgment")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(data))
            );
    }
}
