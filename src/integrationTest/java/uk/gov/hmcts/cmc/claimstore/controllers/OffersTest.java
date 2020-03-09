package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.OffersService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.email.EmailService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api",
        "doc_assembly.url=false"
    }
)
public class OffersTest extends BaseMockSpringTest {
    private static final long CASE_ID = 42L;
    private static final String EXTERNAL_ID = UUID.randomUUID().toString();

    @MockBean
    private ClaimService claimService;
    @MockBean
    protected EmailService emailService;

    @SpyBean
    private OffersService offersService;

    @Before
    public void setUp() {
        given(userService.getUserDetails(AUTHORISATION_TOKEN))
            .willReturn(SampleUserDetails.builder().withRoles(Role.CITIZEN.getRole()).build());
        when(claimService.getClaimByExternalId(EXTERNAL_ID, AUTHORISATION_TOKEN))
            .thenReturn(Claim.builder().build());
    }

    @Test
    public void makeOffer() throws Exception {
        Offer offer = Offer.builder().build();
        MvcResult mvcResult = makePostRequest(MadeBy.CLAIMANT.name(), offer)
            .andExpect(status().isOk())
            .andReturn();

        verify(offersService).makeOffer(any(Claim.class), eq(offer), eq(MadeBy.CLAIMANT), eq(AUTHORISATION_TOKEN));
    }

    private ResultActions makePostRequest(String party, Offer offer) throws Exception {
        return webClient.perform(
            post("/claims/{externalId}/offers/{party}", EXTERNAL_ID, party)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMappingHelper.toJson(offer))
        );
    }
}
