package uk.gov.hmcts.cmc.claimstore;

import org.junit.Before;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import java.time.LocalDate;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class BaseOfferTest extends BaseIntegrationTest {
    protected static final String DEFENDANT_AUTH_TOKEN = "defendant-authDataString";
    protected static final String CLAIMANT_AUTH_TOKEN = "claimant-authDataString";
    protected static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    protected Claim claim;

    @Before
    public void beforeEachTest() throws Exception {
        when(userService.getUserDetails(eq(CLAIMANT_AUTH_TOKEN))).thenReturn(
            SampleUserDetails.builder()
                .withUserId(SUBMITTER_ID)
                .withMail(SampleClaim.SUBMITTER_EMAIL)
                .build()
        );

        when(userService.getUserDetails(eq(DEFENDANT_AUTH_TOKEN))).thenReturn(
            SampleUserDetails.builder()
                .withUserId(DEFENDANT_ID)
                .withMail(SampleClaim.DEFENDANT_EMAIL)
                .build()
        );

        claim = claimStore.saveClaim(SampleClaimData.builder().build(), SUBMITTER_ID, LocalDate.now());
        claimRepository.linkDefendant(claim.getLetterHolderId(), DEFENDANT_ID);
        claim = claimStore.saveResponse(claim, SampleResponse.validDefaults(), DEFENDANT_ID,
            SampleClaim.DEFENDANT_EMAIL);

        prepareDefendantOffer();
    }

    private void prepareDefendantOffer() throws Exception {
        webClient
            .perform(
                post(format("/claims/%s/offers/%s", claim.getExternalId(), MadeBy.DEFENDANT.name()))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, DEFENDANT_AUTH_TOKEN)
                    .content(jsonMapper.toJson(SampleOffer.validDefaults()))
            )
            .andExpect(status().isCreated());
    }

    protected void prepareClaimantOffer() throws Exception {
        webClient
            .perform(
                post(format("/claims/%s/offers/%s", claim.getExternalId(), MadeBy.CLAIMANT.name()))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, CLAIMANT_AUTH_TOKEN)
                    .content(jsonMapper.toJson(SampleOffer.validDefaults()))
            )
            .andExpect(status().isCreated());
    }
}
