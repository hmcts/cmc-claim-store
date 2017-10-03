package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SaveCountyCourtJudgmentTest extends BaseTest {

    private static final long CLAIM_ID = 1L;
    private static final long CLAIMANT_ID = 123L;

    @Before
    public void setup() {
        final UserDetails userDetails
            = SampleUserDetails.builder().withUserId(CLAIMANT_ID).withMail("claimant@email.com").build();

        given(userService.getUserDetails(anyString())).willReturn(userDetails);
        given(pdfServiceClient.generateFromHtml(any(), any())).willReturn(new byte[]{0, 0, 0});
    }

    @Test
    public void shouldReturnClaimWithCountyCourtJudgment() throws Exception {

        Claim claimWithCCJ = SampleClaim.builder()
            .withSubmitterId(CLAIMANT_ID)
            .withResponseDeadline(LocalDate.now().minusDays(2))
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .withPaymentOptionImmediately()
                    .build()
            ).withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
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
                .content(jsonMapper.toJson(SampleCountyCourtJudgment.builder().build()))
            );
    }
}
