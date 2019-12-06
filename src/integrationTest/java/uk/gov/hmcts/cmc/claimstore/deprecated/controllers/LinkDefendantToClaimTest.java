package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUser;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.scheduler.model.JobData;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false",
        "feature_toggles.reminderEmails=true"
    }
)
public class LinkDefendantToClaimTest extends BaseIntegrationTest {

    @Before
    public void init() {
        when(userService.getUserDetails(eq(BEARER_TOKEN))).thenReturn(SampleUserDetails.getDefault());
    }

    @Test
    public void shouldReturn200HttpStatusAndUpdatedClaimWhenLinkIsSuccessfullySet() throws Exception {
        given(userService.getUser(anyString())).willReturn(SampleUser.builder()
            .withAuthorisation(BEARER_TOKEN)
            .withUserDetails(SampleUserDetails.builder()
                .withUserId(DEFENDANT_ID)
                .withMail(DEFENDANT_EMAIL)
                .withRoles("citizen", "letter-" + SampleClaim.LETTER_HOLDER_ID)
                .build())
            .build()
        );

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        webClient
            .perform(put("/claims/defendant/link")
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isOk());

        assertThat(claimStore.getClaim(claim.getId()))
            .extracting(Claim::getId, Claim::getDefendantId)
            .containsExactly(claim.getId(), "555");

        LocalDate responseDeadline = claim.getResponseDeadline();
        ZonedDateTime firstReminderDate = responseDeadline.minusDays(5).atTime(8, 0).atZone(ZoneOffset.UTC);
        ZonedDateTime lastReminderDate = responseDeadline.minusDays(1).atTime(8, 0).atZone(ZoneOffset.UTC);

        verify(jobService).scheduleJob(any(JobData.class), eq(firstReminderDate));
        verify(jobService).scheduleJob(any(JobData.class), eq(lastReminderDate));
    }
}
