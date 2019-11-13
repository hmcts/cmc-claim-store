package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NOTIFICATION_FAILURE;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class MakeOfferTest extends BaseIntegrationTest {

    private static final String DEFENDANT_AUTH_TOKEN = "authDataString";

    private Claim claim;

    @Before
    public void beforeEachTest() {
        claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());

        UserDetails userDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withMail(DEFENDANT_EMAIL)
            .withRoles("letter-" + claim.getLetterHolderId())
            .build();

        when(userService.getUserDetails(DEFENDANT_AUTH_TOKEN)).thenReturn(userDetails);
        given(userService.getUser(DEFENDANT_AUTH_TOKEN)).willReturn(new User(DEFENDANT_AUTH_TOKEN, userDetails));
        caseRepository.linkDefendant(DEFENDANT_AUTH_TOKEN);
    }

    @Test
    public void shouldAcceptValidOfferByDefendantAndReturnCreatedStatus() throws Exception {
        makeOffer(DEFENDANT_AUTH_TOKEN, SampleOffer.builder().build(), MadeBy.DEFENDANT.name())
            .andExpect(status().isCreated())
            .andReturn();
    }

    @Test
    public void shouldReturnBadRequestIfPartyIsIncorrectlySpecified() throws Exception {
        makeOffer(DEFENDANT_AUTH_TOKEN, SampleOffer.builder().build(), "I'm not a valid enum value")
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    public void shouldSendNotifications() throws Exception {
        Offer offer = SampleOffer.builder().build();
        given(notificationClient.sendEmail(any(), any(), any(), any()))
            .willReturn(null);

        makeOffer(DEFENDANT_AUTH_TOKEN, offer, MadeBy.DEFENDANT.name())
            .andExpect(status().isCreated())
            .andReturn();

        verify(notificationClient, times(1))
            .sendEmail(any(), any(), anyMap(), contains("claimant-offer-made-notification-"));
        verify(notificationClient, times(1))
            .sendEmail(any(), any(), anyMap(), contains("defendant-offer-made-notification-"));
    }

    @Test
    public void shouldRetrySendNotifications() throws Exception {
        Offer offer = SampleOffer.builder().build();
        given(notificationClient.sendEmail(any(), any(), any(), any()))
            .willThrow(new NotificationClientException(new RuntimeException("first attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("1st email, 2nd attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("1st email, 3rd attempt fails, stop")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 1st attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 2nd attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 3rd attempt fails, stop")));

        makeOffer(DEFENDANT_AUTH_TOKEN, offer, MadeBy.DEFENDANT.name())
            .andExpect(status().is5xxServerError())
            .andReturn();

        verify(notificationClient, times(3)).sendEmail(any(), any(), anyMap(), anyString());

        verify(appInsights).trackEvent(
            eq(NOTIFICATION_FAILURE),
            eq(REFERENCE_NUMBER),
            anyString()
        );
    }

    private ResultActions makeOffer(String authToken, Offer offer, String party) throws Exception {
        return webClient
            .perform(post(format("/claims/%s/offers/%s", claim.getExternalId(), party))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .content(jsonMapper.toJson(offer))
            );
    }

}
