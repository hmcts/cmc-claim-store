package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.models.offers.MadeBy;
import uk.gov.hmcts.cmc.claimstore.models.offers.Offer;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.offers.SampleOffer;

import static java.lang.String.format;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MakeOfferTest extends BaseTest {

    @Test
    public void shouldAcceptValidOfferByDefendantAndReturnCreatedStatus() throws Exception {
        makeOffer(SampleOffer.validDefaults(), MadeBy.defendant.name())
            .andExpect(status().isCreated())
            .andReturn();
    }

    @Test
    public void shouldReturnBadRequestIfPartyIsIncorrectlySpecified() throws Exception {
        makeOffer(SampleOffer.validDefaults(), "I'm not a valid enum value")
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    private ResultActions makeOffer(Offer offer, String party) throws Exception {
        return webClient
            .perform(post(format("/claims/123/offers/%s", party))
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .content(jsonMapper.toJson(offer))
            );
    }

}
