package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OffersTest extends BaseMockSpringTest {
    private static final String MAKE_OFFER_URL = "/claims/{externalId}/offers/{party}";
    private static final String ACCEPT_OFFER_URL = MAKE_OFFER_URL + "/accept";
    private static final String REJECT_OFFER_URL = MAKE_OFFER_URL + "/reject";
    private static final String COUNTERSIGN_URL = MAKE_OFFER_URL + "/countersign";

    @MockBean
    protected EmailService emailService;

    @MockBean
    protected ClaimService claimService;

    @Before
    public void setup() {
        given(userService.getUserDetails(AUTHORISATION_TOKEN))
            .willReturn(SampleUserDetails.builder().withRoles(Role.CITIZEN.getRole()).build());
        given(authTokenGenerator.generate()).willReturn(SERVICE_TOKEN);
    }

    @Test
    public void shouldMakeOffer() throws Exception {
        Offer offer = SampleOffer.builderWithPaymentIntention().build();
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation();

        postRequest(MAKE_OFFER_URL, claim, MadeBy.DEFENDANT, offer)
            .andExpect(status().isCreated());
    }

    @Test
    public void shouldRefuseOfferWhenAlreadySettled() throws Exception {
        Offer offer = SampleOffer.builderWithPaymentIntention().build();
        Claim claim = SampleClaim.withSettlementReached();

        postRequest(MAKE_OFFER_URL, claim, MadeBy.DEFENDANT, offer)
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldAcceptOffer() throws Exception {
        Settlement settlement = new Settlement();
        settlement.makeOffer(Offer.builder().build(), MadeBy.DEFENDANT, null);
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation().toBuilder()
            .settlement(settlement)
            .build();

        postRequest(ACCEPT_OFFER_URL, claim, MadeBy.CLAIMANT, null)
            .andExpect(status().isCreated());
    }

    @Test
    public void shouldRefuseAcceptWhenAlreadySettled() throws Exception {
        Claim claim = SampleClaim.withSettlementReached();

        postRequest(ACCEPT_OFFER_URL, claim, MadeBy.CLAIMANT, null)
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldRefuseAcceptWhenNoOfferMade() throws Exception {
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation();

        postRequest(ACCEPT_OFFER_URL, claim, MadeBy.CLAIMANT, null)
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldReject() throws Exception {
        Settlement settlement = new Settlement();
        settlement.makeOffer(Offer.builder().build(), MadeBy.DEFENDANT, null);
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation().toBuilder()
            .settlement(settlement)
            .build();

        postRequest(REJECT_OFFER_URL, claim, MadeBy.CLAIMANT, null)
            .andExpect(status().isCreated());
    }

    @Test
    public void shouldRefuseRejectWhenAlreadySettled() throws Exception {
        Claim claim = SampleClaim.withSettlementReached();

        postRequest(REJECT_OFFER_URL, claim, MadeBy.CLAIMANT, null)
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldRefuseRejectWhenNoOfferMade() throws Exception {
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation();

        postRequest(REJECT_OFFER_URL, claim, MadeBy.CLAIMANT, null)
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldCountersign() throws Exception {
        Settlement settlement = new Settlement();
        settlement.makeOffer(Offer.builder().build(), MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation().toBuilder()
            .settlement(settlement)
            .build();

        postRequest(COUNTERSIGN_URL, claim, MadeBy.DEFENDANT, null)
            .andExpect(status().isCreated());
    }

    @Test
    public void shouldRefuseCountersignWhenAlreadySettled() throws Exception {
        Claim claim = SampleClaim.withSettlementReached();

        postRequest(COUNTERSIGN_URL, claim, MadeBy.DEFENDANT, null)
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldRefuseCountersignWhenNoOfferMade() throws Exception {
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation();

        postRequest(COUNTERSIGN_URL, claim, MadeBy.DEFENDANT, null)
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldRefuseCountersignWhenOfferRejected() throws Exception {
        Settlement settlement = new Settlement();
        settlement.makeOffer(Offer.builder().build(), MadeBy.DEFENDANT, null);
        settlement.reject(MadeBy.CLAIMANT, null);
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation().toBuilder()
            .settlement(settlement)
            .build();

        postRequest(COUNTERSIGN_URL, claim, MadeBy.DEFENDANT, null)
            .andExpect(status().isBadRequest());
    }

    private ResultActions postRequest(String url, Claim claim, MadeBy party, Offer offer) throws Exception {
        CCDCase ccdCase = caseMapper.to(claim);
        Map<String, Object> caseData = caseDetailsConverter.convertToMap(ccdCase);
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(CaseDetails.builder()
                .data(caseData)
                .id(1L)
                .state(ClaimState.OPEN.getValue())
                .build())
            .build();

        when(claimService.getClaimByExternalId(claim.getExternalId(), AUTHORISATION_TOKEN))
            .thenReturn(claim);
        when(coreCaseDataApi.startEventForCitizen(anyString(), anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString())).thenReturn(startEventResponse);

        MockHttpServletRequestBuilder requestBuilder = post(url, claim.getExternalId(), party.name())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN);
        if (offer != null) {
            requestBuilder.content(jsonMappingHelper.toJson(offer));
        }
        return webClient.perform(requestBuilder);
    }
}
