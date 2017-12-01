package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.services.OffersService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponseData;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AcceptOrRejectOfferTest extends BaseIntegrationTest {

    private static final String DEFENDANT_AUTH_TOKEN = "defendant-authDataString";
    private static final String CLAIMANT_AUTH_TOKEN = "claimant-authDataString";

    @SpyBean
    private OffersService offersService;

    private Claim claim;

    @Before
    public void beforeEachTest() throws Exception {
        when(userService.getUserDetails(eq(CLAIMANT_AUTH_TOKEN))).thenReturn(
            SampleUserDetails.builder()
                .withUserId(SUBMITTER_ID)
                .build()
        );

        when(userService.getUserDetails(eq(DEFENDANT_AUTH_TOKEN))).thenReturn(
            SampleUserDetails.builder()
                .withUserId(DEFENDANT_ID)
                .build()
        );

        claim = claimStore.saveClaim(SampleClaimData.builder().build(), SUBMITTER_ID, LocalDate.now());
        claimRepository.linkDefendant(claim.getId(), DEFENDANT_ID);
        claimStore.saveResponse(claim.getId(), SampleResponseData.validDefaults(), DEFENDANT_ID,
            SampleClaim.DEFENDANT_EMAIL);
        prepareDefendantOffer();
    }

    @Test
    public void shouldAcceptExistingOfferAndReturn201Status() throws Exception {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(new byte[]{1, 2, 3, 4});

        postRequestTo("accept")
            .andExpect(status().isCreated());

        verify(offersService).accept(any(Claim.class), eq(MadeBy.CLAIMANT));

        Claim claimWithAcceptedOffer = claimStore.getClaim(claim.getId());
        assertThat(claimWithAcceptedOffer.getSettlementReachedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void shouldRejectExistingOfferAndReturn201Status() throws Exception {
        postRequestTo("reject")
            .andExpect(status().isCreated());

        verify(offersService).reject(any(Claim.class), eq(MadeBy.CLAIMANT));

        Claim claimWithRejectedOffer = claimStore.getClaim(claim.getId());
        assertThat(claimWithRejectedOffer.getSettlementReachedAt()).isNull();
    }

    @Test
    public void shouldAcceptOfferAndSendNotifications() throws Exception {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(new byte[]{1, 2, 3, 4});

        runTestAndVerifyNotificationsAreSentWhenEverythingIsOkForResponse("accept");
    }

    @Test
    public void shouldRejectOfferAndSendNotifications() throws Exception {
        runTestAndVerifyNotificationsAreSentWhenEverythingIsOkForResponse("reject");
    }

    private void runTestAndVerifyNotificationsAreSentWhenEverythingIsOkForResponse(
        final String action
    ) throws Exception {
        given(notificationClient.sendEmail(any(), any(), any(), any()))
            .willReturn(null);

        postRequestTo(action)
            .andExpect(status().isCreated());

        verify(notificationClient, times(1))
            .sendEmail(any(), any(), anyMap(), contains("claimant-offer-made-notification-"));
        verify(notificationClient, times(1))
            .sendEmail(any(), any(), anyMap(), contains("defendant-offer-made-notification-"));

        verify(notificationClient, times(1))
            .sendEmail(
                any(), any(), anyMap(), contains(format("to-claimant-offer-%sed-by-claimant-notification-", action))
            );

        verify(notificationClient, times(1))
            .sendEmail(
                any(), any(), anyMap(), contains(format("to-defendant-offer-%sed-by-claimant-notification-", action))
            );
    }

    private ResultActions postRequestTo(final String endpoint) throws Exception {
        return webClient
            .perform(
                post(format("/claims/%d/offers/%s/%s", claim.getId(), MadeBy.CLAIMANT.name(), endpoint))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, CLAIMANT_AUTH_TOKEN)
            );
    }

    private void prepareDefendantOffer() throws Exception {
        webClient
            .perform(
                post(format("/claims/%d/offers/%s", claim.getId(), MadeBy.DEFENDANT.name()))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, DEFENDANT_AUTH_TOKEN)
                    .content(jsonMapper.toJson(SampleOffer.validDefaults()))
            )
            .andExpect(status().isCreated());
    }
}
