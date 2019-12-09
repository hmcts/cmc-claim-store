package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseOfferTest;
import uk.gov.hmcts.cmc.claimstore.services.OffersService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
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
public class AcceptOrRejectOfferTest extends BaseOfferTest {

    @SpyBean
    private OffersService offersService;

    @Test
    public void shouldAcceptExistingOfferAndReturn201Status() throws Exception {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(new byte[]{1, 2, 3, 4});

        postRequestTo("accept")
            .andExpect(status().isCreated());

        verify(offersService).accept(any(Claim.class), eq(MadeBy.CLAIMANT), eq(CLAIMANT_AUTH_TOKEN));
    }

    @Test
    public void shouldRejectExistingOfferAndReturn201Status() throws Exception {
        postRequestTo("reject")
            .andExpect(status().isCreated());

        verify(offersService).reject(any(Claim.class), eq(MadeBy.CLAIMANT), eq(CLAIMANT_AUTH_TOKEN));

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
        String action
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

    private ResultActions postRequestTo(String endpoint) throws Exception {
        return webClient
            .perform(
                post(format("/claims/%s/offers/%s/%s", claim.getExternalId(), MadeBy.CLAIMANT.name(), endpoint))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, CLAIMANT_AUTH_TOKEN)
            );
    }
}
