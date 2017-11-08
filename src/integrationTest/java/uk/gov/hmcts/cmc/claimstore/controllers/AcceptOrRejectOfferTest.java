package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.offers.SampleOffer;
import uk.gov.hmcts.cmc.claimstore.services.OffersService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import java.time.LocalDate;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AcceptOrRejectOfferTest extends BaseIntegrationTest {

    private static final String DEFENDANT_AUTH_TOKEN = "defendant-authDataString";
    private static final String CLAIMANT_AUTH_TOKEN = "claimant-authDataString";

    @SpyBean
    private OffersService offersService;

    private Claim claim;

    @Before
    public void beforeEachTest() {
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
        prepareDefendantOffer();
    }

    @Test
    public void shouldAcceptExistingOfferAndReturn201Status() throws Exception {
        webClient
            .perform(
                post(format("/claims/%d/offers/%s/accept", claim.getId(), MadeBy.CLAIMANT.name().toLowerCase()))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, CLAIMANT_AUTH_TOKEN)
            )
            .andExpect(status().isCreated());

        verify(offersService).accept(any(Claim.class), eq(MadeBy.CLAIMANT));
    }

    @Test
    public void shouldRejectExistingOfferAndReturn201Status() throws Exception {
        webClient
            .perform(
                post(format("/claims/%d/offers/%s/reject", claim.getId(), MadeBy.CLAIMANT.name().toLowerCase()))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, CLAIMANT_AUTH_TOKEN)
            )
            .andExpect(status().isCreated());

        verify(offersService).reject(any(Claim.class), eq(MadeBy.CLAIMANT));
    }

    private void prepareDefendantOffer() {

        final String url = format("/claims/%d/offers/%s", claim.getId(), MadeBy.DEFENDANT.name().toLowerCase());
        final String requestBody = jsonMapper.toJson(SampleOffer.validDefaults());

        try {
            webClient
                .perform(
                    post(url)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, DEFENDANT_AUTH_TOKEN)
                        .content(requestBody)
                )
                .andExpect(status().isCreated());
        } catch (Exception e) {
            Assert.fail(format("I was unable to create test data (POST: %s %s)", url, requestBody));
        }
    }
}
