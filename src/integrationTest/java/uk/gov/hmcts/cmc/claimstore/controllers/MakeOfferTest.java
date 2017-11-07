package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.exceptions.IllegalSettlementStatementException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;
import uk.gov.hmcts.cmc.claimstore.models.offers.Offer;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.offers.SampleOffer;
import uk.gov.hmcts.cmc.claimstore.services.OffersService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MakeOfferTest extends BaseIntegrationTest {

    private static final String DEFENDANT_AUTH_TOKEN = "authDataString";

    @SpyBean
    private OffersService offersService;

    private Claim claim;

    @Before
    public void beforeEachTest() {
        claim = claimStore.saveClaim(SampleClaimData.builder().build(), "1", LocalDate.now());
        claimRepository.linkDefendant(claim.getId(), DEFENDANT_ID);

        when(userService.getUserDetails(DEFENDANT_AUTH_TOKEN)).thenReturn(
            SampleUserDetails.builder()
                .withUserId(DEFENDANT_ID)
                .build()
        );
    }

    @Test
    public void shouldAcceptValidOfferByDefendantAndReturnCreatedStatus() throws Exception {
        makeOffer(DEFENDANT_AUTH_TOKEN, SampleOffer.validDefaults(), MadeBy.DEFENDANT.name().toLowerCase())
            .andExpect(status().isCreated())
            .andReturn();
    }

    @Test
    public void shouldReturnBadRequestIfPartyIsIncorrectlySpecified() throws Exception {
        makeOffer(DEFENDANT_AUTH_TOKEN, SampleOffer.validDefaults(), "I'm not a valid enum value")
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    public void shouldReturnForbiddenIfUserIsNotPartyOnClaim() throws Exception {
        when(userService.getUserDetails(DEFENDANT_AUTH_TOKEN)).thenReturn(
            SampleUserDetails.builder()
                .withUserId("Not an ID on the claim")
                .build()
        );

        makeOffer(DEFENDANT_AUTH_TOKEN, SampleOffer.validDefaults(), MadeBy.DEFENDANT.name())
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    public void shouldReturnForbiddenIfUserIsPartyOnClaimButClaimsToBeOppositeParty() throws Exception {
        makeOffer(DEFENDANT_AUTH_TOKEN, SampleOffer.validDefaults(), MadeBy.CLAIMANT.name())
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    public void shouldReturnBadRequestIfIllegalSettlementStatementIsSubmitted() throws Exception {
        Offer offer = SampleOffer.validDefaults();
        doThrow(new IllegalSettlementStatementException("Invalid statement was mode"))
            .when(offersService).makeOffer(any(Claim.class), eq(offer), eq(MadeBy.DEFENDANT));

        makeOffer(DEFENDANT_AUTH_TOKEN, offer, MadeBy.DEFENDANT.name())
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    public void shouldSendNotifications() throws Exception {
        Offer offer = SampleOffer.validDefaults();
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
        Offer offer = SampleOffer.validDefaults();
        given(notificationClient.sendEmail(any(), any(), any(), any()))
            .willThrow(new NotificationClientException(new RuntimeException("first attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("1st email, 2nd attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("1st email, 3rd attempt fails, stop")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 1st attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 2nd attempt fails")))
            .willThrow(new NotificationClientException(new RuntimeException("2nd email, 3rd attempt fails, stop")));

        makeOffer(DEFENDANT_AUTH_TOKEN, offer, MadeBy.DEFENDANT.name())
            .andExpect(status().isCreated())
            .andReturn();

        verify(notificationClient, times(3))
            .sendEmail(any(), any(), anyMap(), contains("claimant-offer-made-notification-"));
        verify(notificationClient, times(3))
            .sendEmail(any(), any(), anyMap(), contains("defendant-offer-made-notification-"));
    }

    private ResultActions makeOffer(String authToken, Offer offer, String party) throws Exception {
        return webClient
            .perform(post(format("/claims/%d/offers/%s", claim.getId(), party))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .content(jsonMapper.toJson(offer))
            );
    }

}
