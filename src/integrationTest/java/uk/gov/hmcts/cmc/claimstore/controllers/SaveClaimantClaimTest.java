package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.SUBMITTER_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.RESPONSE_DEADLINE;

public class SaveClaimantClaimTest extends BaseTest {

    private static final long CLAIM_ID = 1L;
    private static final long CLAIMANT_ID = 123L;
    private static final Long LETTER_HOLDER_ID = 1L;
    private static final Long DEFENDANT_ID = 2L;
    private static final String REFERENCE_NUMBER = "000MC001";
    private static final String PIN = "my-pin";
    private static final String EXTERNAL_ID = "external-id";
    private static final boolean DEADLINE_NOT_UPDATED = false;

    private final Claim claimAfterSaving = new Claim(
        CLAIM_ID,
        CLAIMANT_ID,
        LETTER_HOLDER_ID,
        DEFENDANT_ID,
        EXTERNAL_ID,
        REFERENCE_NUMBER,
        SampleClaimData.submittedByClaimant(),
        NOW_IN_LOCAL_ZONE,
        ISSUE_DATE,
        RESPONSE_DEADLINE,
        DEADLINE_NOT_UPDATED,
        SUBMITTER_EMAIL,
        null,
        null,
        null,
        null,
        null,
        null,
        null);

    @Before
    public void setup() {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(new byte[]{1, 2, 3, 4});

        given(claimRepository.saveSubmittedByClaimant(anyString(), anyLong(), anyLong(), any(LocalDate.class),
            any(LocalDate.class), anyString(), anyString()))
            .willReturn(CLAIM_ID);

        given(claimRepository.getById(CLAIM_ID)).willReturn(Optional.of(claimAfterSaving));

        given(userService.generatePin(anyString(), anyString()))
            .willReturn(new GeneratePinResponse(PIN, LETTER_HOLDER_ID));

        given(userService.getUserDetails(anyString())).willReturn(
            SampleUserDetails.builder().withUserId(CLAIMANT_ID).withMail("claimant@email.com").build());

        given(holidaysCollection.getPublicHolidays()).willReturn(emptySet());
    }

    @Test
    public void shouldReturnNewlyCreatedClaim() throws Exception {
        //when
        MvcResult result = postClaim(SampleClaimData.submittedByClaimant())
            .andExpect(status().isOk())
            .andReturn();

        //then
        final Claim returnedClaim = jsonMapper.fromJson(result.getResponse().getContentAsString(), Claim.class);
        assertThat(returnedClaim).isEqualTo(claimAfterSaving);
    }

    @Test
    public void claimSaveShouldFailWhenStaffNotificationFails() throws Exception {
        //given
        doThrow(new RuntimeException("Sending failed"))
            .when(emailService).sendEmail(anyString(), any(EmailData.class));

        //when, then
        postClaim(SampleClaimData.submittedByClaimant())
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

    @Test
    public void claimSaveShouldFailWhenDuplicateExternalId() throws Exception {
        //given
        given(claimRepository.getClaimByExternalId(any())).willReturn(Optional.of(claimAfterSaving));
        //when, then
        postClaim(SampleClaimData.validDefaults())
            .andExpect(status().isConflict())
            .andReturn();
    }

    @Test
    public void claimSaveShouldRetrySendNotifications() throws Exception {
        //given
        given(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email1")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email2")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email3")));

        //when
        postClaim(SampleClaimData.submittedByClaimant())
            .andExpect(status().isOk())
            .andReturn();

        //verify
        verify(notificationClient, atLeast(3))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());

    }

    @Test
    public void claimSaveShouldNotFailWhenCitizenNotificationFails() throws Exception {
        //given
        given(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email")));

        //when
        MvcResult result = postClaim(SampleClaimData.submittedByClaimant())
            .andExpect(status().isOk())
            .andReturn();

        //then
        final Claim returnedClaim = jsonMapper.fromJson(result.getResponse().getContentAsString(), Claim.class);
        assertThat(returnedClaim).isEqualTo(claimAfterSaving);
    }

    private ResultActions postClaim(ClaimData claimData) throws Exception {
        return webClient
            .perform(post("/claims/" + (Long) 123L)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(claimData))
            );
    }
}
