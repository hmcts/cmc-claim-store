package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleResponseData;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

public class RequestMoreTimeForResponseTest extends BaseTest {

    private static final String AUTH_TOKEN = "it's me!";
    private static final String DEFENDANT_ID = "100";
    private static final LocalDate RESPONSE_DEADLINE = LocalDate.now().plusDays(10);

    private static final UserDetails USER_DETAILS
        = SampleUserDetails.builder().withUserId(DEFENDANT_ID).withMail("myemail@example.com").build();

    private static final UserDetails OTHER_USER_DETAILS
        = SampleUserDetails.builder().withUserId(SUBMITTER_ID).withMail("other@example.com").build();

    private static final boolean MORE_TIME_ALREADY_REQUESTED = true;
    private static final boolean MORE_TIME_NOT_REQUESTED_YET = false;

    @Test
    public void shouldUpdatedResponseDeadlineWhenEverythingIsOk() throws Exception {

        mockHappyPath();

        webClient
            .perform(callPost(CLAIM_ID))
            .andExpect(status().isOk())
            .andReturn();

        verify(claimRepository, once()).requestMoreTime(eq(CLAIM_ID), any(LocalDate.class));
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOk() throws Exception {

        mockHappyPath();
        given(notificationClient.sendEmail(any(), any(), any(), any())).willReturn(null);

        webClient
            .perform(callPost(CLAIM_ID))
            .andExpect(status().isOk())
            .andReturn();

        verify(notificationClient, times(3))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    public void shouldRetrySendNotifications() throws Exception {

        mockHappyPath();

        given(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .willThrow(new NotificationClientException(new RuntimeException("first attempt fails")))
            .willReturn(null) // first notification sent successfully on second attempt
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 1st attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 2nd attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 3rd attempt fails, stop")));

        webClient
            .perform(callPost(CLAIM_ID))
            .andExpect(status().isOk())
            .andReturn();

        verify(notificationClient, times(8))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    public void shouldReturn404HttpStatusWhenClaimDoesNotExist() throws Exception {

        final long nonExistingClaim = -1L;

        given(claimRepository.getById(nonExistingClaim)).willReturn(Optional.empty());
        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(USER_DETAILS);

        webClient
            .perform(callPost(nonExistingClaim))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldReturn500HttpStatusWhenFailedToRetrieveClaim() throws Exception {

        given(claimRepository.getById(CLAIM_ID))
            .willThrow(new UnableToExecuteStatementException("Unexpected error", (StatementContext) null));

        webClient
            .perform(callPost(CLAIM_ID))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

    @Test
    public void shouldReturn403HttpStatusWhenUserIsNotLinkedWithClaim() throws Exception {

        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(OTHER_USER_DETAILS);
        given(claimRepository.getById(CLAIM_ID))
            .willReturn(Optional.of(createClaimModel(MORE_TIME_NOT_REQUESTED_YET)));

        webClient
            .perform(callPost(CLAIM_ID))
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    public void shouldReturn409HttpStatusWhenItsTooLateToRequestForMoreTime() throws Exception {

        final LocalDate responseDeadlineInThePast = LocalDate.now().minusDays(10);

        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(USER_DETAILS);
        given(claimRepository.getById(CLAIM_ID))
            .willReturn(Optional.of(createClaimModel(responseDeadlineInThePast, MORE_TIME_NOT_REQUESTED_YET)));

        webClient
            .perform(callPost(CLAIM_ID))
            .andExpect(status().isConflict())
            .andReturn();
    }

    @Test
    public void shouldReturn409HttpStatusWhenUserIsTryingToRequestForMoreTimeAgain() throws Exception {

        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(USER_DETAILS);
        given(claimRepository.getById(CLAIM_ID))
            .willReturn(Optional.of(createClaimModel(MORE_TIME_ALREADY_REQUESTED)));

        webClient
            .perform(callPost(CLAIM_ID))
            .andExpect(status().isConflict())
            .andReturn();
    }

    @Test
    public void shouldReturn400HttpStatusWhenNoHeadersSet() throws Exception {
        webClient
            .perform(post("/claims/" + CLAIM_ID + "/request-more-time"))
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    public void shouldReturn400HttpStatusWhenNoAuthorizationHeaderSet() throws Exception {
        webClient
            .perform(post("/claims/" + CLAIM_ID + "/request-more-time")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    private MockHttpServletRequestBuilder callPost(final long claimId) {
        return post("/claims/" + claimId + "/request-more-time")
            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN);
    }

    private void mockHappyPath() throws NotificationClientException {
        given(claimRepository.getById(CLAIM_ID))
            .willReturn(Optional.of(createClaimModel(MORE_TIME_NOT_REQUESTED_YET)))
            .willReturn(Optional.of(createClaimModel(MORE_TIME_ALREADY_REQUESTED)));

        given(userService.getUserDetails(AUTH_TOKEN))
            .willReturn(USER_DETAILS);
    }

    private Claim createClaimModel(final boolean updated) {
        return createClaimModel(RESPONSE_DEADLINE, updated);
    }

    private Claim createClaimModel(final LocalDate responseDeadline, final boolean alreadyUpdated) {
        return SampleClaim.builder()
            .withClaimId(CLAIM_ID)
            .withSubmitterEmail("submitter@example.com")
            .withSubmitterId(SUBMITTER_ID)
            .withLetterHolderId(LETTER_HOLDER_ID)
            .withDefendantId(DEFENDANT_ID)
            .withReferenceNumber(REFERENCE_NUMBER)
            .withClaimData(SampleClaimData.validDefaults())
            .withCreatedAt(LocalDateTime.now())
            .withIssuedOn(LocalDate.now())
            .withResponseDeadline(responseDeadline)
            .withMoreTimeRequested(alreadyUpdated)
            .withResponse(SampleResponseData.validDefaults())
            .build();
    }
}
