package uk.gov.hmcts.cmc.claimstore.controllers;

import org.assertj.core.util.Maps;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
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

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class RequestMoreTimeForResponseTest extends BaseIntegrationTest {

    private static final String DEFENDANT_ID = "100";

    private static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withUserId(DEFENDANT_ID)
        .withMail("defendant@example.com")
        .build();

    @Test
    @Ignore("Disabled due to using CCD")
    public void shouldUpdatedResponseDeadlineWhenEverythingIsOkV1() throws Exception {
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);

        makeRequest(claim.getExternalId())
            .andExpect(status().isOk())
            .andReturn();

        Claim updatedClaim = claimRepository.getById(claim.getId()).orElseThrow(RuntimeException::new);

        assertThat(updatedClaim.isMoreTimeRequested()).isTrue();
    }

    @Test
    public void shouldUpdatedResponseDeadlineWhenEverythingIsOkV2() throws Exception {
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        caseRepository.linkDefendantV2(BEARER_TOKEN);

        makeRequest(claim.getExternalId())
            .andExpect(status().isOk())
            .andReturn();

        Claim updatedClaim = claimRepository.getById(claim.getId()).orElseThrow(RuntimeException::new);

        assertThat(updatedClaim.isMoreTimeRequested()).isTrue();
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOkV1() throws Exception {
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);

        makeRequest(claim.getExternalId())
            .andExpect(status().isOk());

        verify(notificationClient, times(3))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOkV2() throws Exception {
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        caseRepository.linkDefendantV2(BEARER_TOKEN);

        makeRequest(claim.getExternalId())
            .andExpect(status().isOk());

        verify(notificationClient, times(3))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }


    @Test
    @Ignore("Disabled due to using CCD")
    public void shouldRetrySendNotificationsV1() throws Exception {
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);

        given(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .willThrow(new NotificationClientException(new RuntimeException("first attempt fails")))
            .willReturn(null) // first notification sent successfully on second attempt
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 1st attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 2nd attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 3rd attempt fails, stop")));

        makeRequest(claim.getExternalId())
            .andExpect(status().isOk());

        verify(notificationClient, times(8))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    public void shouldRetrySendNotificationsV2() throws Exception {
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        caseRepository.linkDefendantV2(BEARER_TOKEN);

        given(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .willThrow(new NotificationClientException(new RuntimeException("first attempt fails")))
            .willReturn(null) // first notification sent successfully on second attempt
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 1st attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 2nd attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 3rd attempt fails, stop")));

        makeRequest(claim.getExternalId())
            .andExpect(status().isOk());

        verify(notificationClient, times(8))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }


    @Test
    public void shouldReturn404HttpStatusWhenClaimDoesNotExist() throws Exception {
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

        String nonExistingClaim = "84f1dda3-e205-4277-96a6-1f23b6f1766d";

        makeRequest(nonExistingClaim)
            .andExpect(status().isNotFound());
    }

    @Test
    @Ignore("Disabled due to using CCD")
    public void shouldReturn409HttpStatusWhenItsTooLateToRequestForMoreTimeV1() throws Exception {
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

        LocalDate responseDeadlineInThePast = LocalDate.now().minusDays(10);

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", responseDeadlineInThePast);
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);

        makeRequest(claim.getExternalId())
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturn409HttpStatusWhenItsTooLateToRequestForMoreTimeV2() throws Exception {
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

        LocalDate responseDeadlineInThePast = LocalDate.now().minusDays(10);

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", responseDeadlineInThePast);
        caseRepository.linkDefendantV2(BEARER_TOKEN);

        makeRequest(claim.getExternalId())
            .andExpect(status().isConflict());
    }

    @Test
    @Ignore("Disabled due to using CCD")
    public void shouldReturn409HttpStatusWhenUserIsTryingToRequestForMoreTimeAgainV1() throws Exception {
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        caseRepository.linkDefendantV1(claim.getExternalId(), DEFENDANT_ID, BEARER_TOKEN);
        claimRepository.requestMoreTime(claim.getExternalId(), LocalDate.now());

        makeRequest(claim.getExternalId())
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturn409HttpStatusWhenUserIsTryingToRequestForMoreTimeAgainV2() throws Exception {
        given(userService.getUserDetails(BEARER_TOKEN)).willReturn(USER_DETAILS);

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        caseRepository.linkDefendantV2(BEARER_TOKEN);
        claimRepository.requestMoreTime(claim.getExternalId(), LocalDate.now());

        makeRequest(claim.getExternalId())
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturn400HttpStatusWhenNoAuthorizationHeaderSet() throws Exception {
        makeRequest("84f1dda3-e205-4277-96a6-1f23b6f1766d", new HashMap<>())
            .andExpect(status().isBadRequest());
    }

    private ResultActions makeRequest(String externalId) throws Exception {
        return makeRequest(externalId, Maps.newHashMap(HttpHeaders.AUTHORIZATION, BEARER_TOKEN));
    }

    private ResultActions makeRequest(String externalId, Map<String, String> headers) throws Exception {
        MockHttpServletRequestBuilder builder = post("/claims/" + externalId + "/request-more-time");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }

        return webClient.perform(builder);
    }
}
