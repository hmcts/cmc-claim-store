package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.exceptions.IllegalSettlementStatementException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;
import uk.gov.hmcts.cmc.claimstore.models.offers.Offer;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.offers.SampleOffer;
import uk.gov.hmcts.cmc.claimstore.services.OffersService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;

import java.util.Optional;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MakeOfferTest extends BaseTest {

    private static final Long CLAIM_ID = 12344321L;
    private static final String DEFENDANT_ID = "43211234";
    private static final String AUTH_TOKEN = "authDataString";

    @SpyBean
    private OffersService offersService;

    @Before
    public void beforeEachTest() {
        when(claimRepository.getById(CLAIM_ID)).thenReturn(Optional.of(
            SampleClaim.builder()
                .withDefendantId(DEFENDANT_ID)
                .build()
        ));

        when(userService.getUserDetails(AUTH_TOKEN)).thenReturn(
            SampleUserDetails.builder()
                .withUserId(DEFENDANT_ID)
                .build()
        );
    }

    @Test
    public void shouldAcceptValidOfferByDefendantAndReturnCreatedStatus() throws Exception {
        makeOffer(SampleOffer.validDefaults(), MadeBy.DEFENDANT.name())
            .andExpect(status().isCreated())
            .andReturn();
    }

    @Test
    public void shouldReturnBadRequestIfPartyIsIncorrectlySpecified() throws Exception {
        makeOffer(SampleOffer.validDefaults(), "I'm not a valid enum value")
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    public void shouldReturnForbiddenIfUserIsNotPartyOnClaim() throws Exception {
        when(userService.getUserDetails(AUTH_TOKEN)).thenReturn(
            SampleUserDetails.builder()
                .withUserId("-300")
                .build()
        );

        makeOffer(SampleOffer.validDefaults(), MadeBy.DEFENDANT.name())
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    public void shouldReturnForbiddenIfUserIsPartyOnClaimButClaimsToBeOppositeParty() throws Exception {
        makeOffer(SampleOffer.validDefaults(), MadeBy.CLAIMANT.name())
            .andExpect(status().isForbidden())
            .andReturn();
    }

    @Test
    public void shouldReturnBadRequestIfIllegalSettlementStatementIsSubmitted() throws Exception {
        Offer offer = SampleOffer.validDefaults();
        doThrow(new IllegalSettlementStatementException("Invalid statement was mode"))
            .when(offersService).makeOffer(any(Claim.class), eq(offer), eq(MadeBy.DEFENDANT));

        makeOffer(offer, MadeBy.DEFENDANT.name())
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    private ResultActions makeOffer(Offer offer, String party) throws Exception {
        return webClient
            .perform(post(format("/claims/%d/offers/%s", CLAIM_ID, party))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .content(jsonMapper.toJson(offer))
            );
    }

}
