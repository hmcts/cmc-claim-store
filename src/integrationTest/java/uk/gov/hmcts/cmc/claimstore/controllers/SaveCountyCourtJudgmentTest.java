package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SaveCountyCourtJudgmentTest extends BaseTest {

    private static final long CLAIM_ID = 1L;
    private static final long CLAIMANT_ID = 123L;
    private static final Long LETTER_HOLDER_ID = 1L;
    private static final Long DEFENDANT_ID = 2L;
    private static final String REFERENCE_NUMBER = "000MC001";
    private static final String PIN = "my-pin";
    private static final String EXTERNAL_ID = "external-id";
    private static final boolean DEADLINE_NOT_UPDATED = false;

    @Before
    public void setup() {

        given(userService.getUserDetails(anyString()))
            .willReturn(new UserDetails(CLAIMANT_ID, "claimant@email.com"));
    }

    @Test
    public void shouldReturnClaimWithCountyCourtJudgment() throws Exception {

        Claim claimWithCCJ = SampleClaim.builder()
            .withSubmitterId(CLAIMANT_ID)
            .withResponseDeadline(LocalDate.now().minusDays(2))
            .withCountyCourtJudgment(new HashMap<>())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        //given
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
            .perform(post("/claims/county-court-judgment/" + claimId)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(data))
            );
    }
}
