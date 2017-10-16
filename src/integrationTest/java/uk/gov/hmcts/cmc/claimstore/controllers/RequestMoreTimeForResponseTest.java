package uk.gov.hmcts.cmc.claimstore.controllers;

import org.assertj.core.util.Maps;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RequestMoreTimeForResponseTest extends BaseTest {

    private static final String AUTH_TOKEN = "it's me!";

    private static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withUserId(DEFENDANT_ID)
        .withMail("defendant@example.com")
        .build();

    private static final UserDetails OTHER_USER_DETAILS = SampleUserDetails.builder()
        .withUserId(SUBMITTER_ID)
        .withMail("submitter@example.com")
        .build();

    @Test
    public void shouldUpdatedResponseDeadlineWhenEverythingIsOk() throws Exception {
        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.save(SampleClaimData.builder().build());
        claimRepository.linkDefendant(claim.getId(), DEFENDANT_ID);

        webClient
            .perform(request(claim.getId()))
            .andExpect(status().isOk())
            .andReturn();

        Claim updatedClaim = claimRepository.getById(claim.getId()).orElseThrow(RuntimeException::new);

        assertThat(updatedClaim.isMoreTimeRequested()).isTrue();
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOk() throws Exception {
        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.save(SampleClaimData.builder().build());
        claimRepository.linkDefendant(claim.getId(), DEFENDANT_ID);

        webClient
            .perform(request(claim.getId()))
            .andExpect(status().isOk());

        verify(notificationClient, times(3))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    public void shouldRetrySendNotifications() throws Exception {
        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.save(SampleClaimData.builder().build());
        claimRepository.linkDefendant(claim.getId(), DEFENDANT_ID);

        given(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .willThrow(new NotificationClientException(new RuntimeException("first attempt fails")))
            .willReturn(null) // first notification sent successfully on second attempt
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 1st attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 2nd attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 3rd attempt fails, stop")));

        webClient
            .perform(request(claim.getId()))
            .andExpect(status().isOk());

        verify(notificationClient, times(8))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    public void shouldReturn404HttpStatusWhenClaimDoesNotExist() throws Exception {
        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(USER_DETAILS);

        long nonExistingClaim = 900L;

        webClient
            .perform(request(nonExistingClaim))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn403HttpStatusWhenUserIsNotLinkedWithClaim() throws Exception {
        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(OTHER_USER_DETAILS);

        Claim claim = claimStore.save(SampleClaimData.builder().build());
        claimRepository.linkDefendant(claim.getId(), DEFENDANT_ID);

        webClient
            .perform(request(claim.getId()))
            .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn409HttpStatusWhenItsTooLateToRequestForMoreTime() throws Exception {
        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(USER_DETAILS);

        LocalDate responseDeadlineInThePast = LocalDate.now().minusDays(10);

        Claim claim = claimStore.save(SampleClaimData.builder().build(), 1L, responseDeadlineInThePast);
        claimRepository.linkDefendant(claim.getId(), DEFENDANT_ID);

        webClient
            .perform(request(claim.getId()))
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturn409HttpStatusWhenUserIsTryingToRequestForMoreTimeAgain() throws Exception {
        given(userService.getUserDetails(AUTH_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.save(SampleClaimData.builder().build());
        claimRepository.linkDefendant(claim.getId(), DEFENDANT_ID);
        claimRepository.requestMoreTime(claim.getId(), LocalDate.now());

        webClient
            .perform(request(claim.getId()))
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturn400HttpStatusWhenNoAuthorizationHeaderSet() throws Exception {
        webClient
            .perform(request(900L, new HashMap<>()))
            .andExpect(status().isBadRequest());
    }

    private MockHttpServletRequestBuilder request(final long claimId) {
        return request(claimId, Maps.newHashMap(HttpHeaders.AUTHORIZATION, AUTH_TOKEN));
    }

    private MockHttpServletRequestBuilder request(final long claimId, Map<String, String> headers) {
        MockHttpServletRequestBuilder builder = post("/claims/" + claimId + "/request-more-time");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }

        return builder;
    }
}
