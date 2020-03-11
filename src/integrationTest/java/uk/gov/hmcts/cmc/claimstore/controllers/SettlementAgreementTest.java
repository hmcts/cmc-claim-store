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
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SettlementAgreementTest extends BaseMockSpringTest {
    private static final String BASE_SETTLEMENT_URL = "/claims/{externalId}/settlement-agreement";
    private static final String REJECT_SETTLEMENT_URL = BASE_SETTLEMENT_URL + "/reject";
    private static final String COUNTERSIGN_SETTLEMENT_URL = BASE_SETTLEMENT_URL + "/countersign";

    @MockBean
    protected ClaimService claimService;

    @MockBean
    protected EmailService emailService;

    @Before
    public void setUp() {
        given(userService.getUserDetails(AUTHORISATION_TOKEN))
            .willReturn(SampleUserDetails.builder().withRoles(Role.CITIZEN.getRole()).build());
        given(authTokenGenerator.generate())
            .willReturn(SERVICE_TOKEN);
    }

    @Test
    public void shouldRejectSettlement() throws Exception {
        Settlement settlement = new Settlement();
        settlement.makeOffer(Offer.builder().build(), MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        Claim claim = SampleClaim.getClaimWithFullAdmission().toBuilder()
            .claimantRespondedAt(LocalDateTime.now())
            .claimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation.builder()
                .buildAcceptationIssueSettlementWithDefendantPaymentIntention())
            .settlement(settlement)
            .build();

        postRequest(REJECT_SETTLEMENT_URL, claim)
            .andExpect(status().isCreated());
    }

    @Test
    public void shouldFailRejectSettlementWhenSettlementAlreadyReached() throws Exception {
        Claim claim = SampleClaim.withSettlementReached();

        postRequest(REJECT_SETTLEMENT_URL, claim)
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldFailRejectSettlementWhenClaimantRejected() throws Exception {
        Settlement settlement = new Settlement();
        settlement.makeOffer(Offer.builder().build(), MadeBy.DEFENDANT, null);
        settlement.reject(MadeBy.CLAIMANT, null);

        Claim claim = SampleClaim.getClaimWithFullAdmission().toBuilder()
            .claimantRespondedAt(LocalDateTime.now())
            .claimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation.builder()
                .buildAcceptationIssueSettlementWithDefendantPaymentIntention())
            .settlement(settlement)
            .build();

        postRequest(REJECT_SETTLEMENT_URL, claim)
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldCountersignSettlement() throws Exception {
        Settlement settlement = new Settlement();
        settlement.makeOffer(Offer.builder().build(), MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        Claim claim = SampleClaim.getClaimWithFullAdmission().toBuilder()
            .claimantRespondedAt(LocalDateTime.now())
            .claimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation.builder()
                .buildAcceptationIssueSettlementWithDefendantPaymentIntention())
            .settlement(settlement)
            .build();

        postRequest(COUNTERSIGN_SETTLEMENT_URL, claim)
            .andExpect(status().isCreated());
    }

    @Test
    public void shouldRefuseCountersignSettlementWhenSettlementAlreadyReached() throws Exception {
        Claim claim = SampleClaim.withSettlementReached();

        postRequest(COUNTERSIGN_SETTLEMENT_URL, claim)
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldRefuseCountersignSettlementWhenClaimantRejected() throws Exception {
        Settlement settlement = new Settlement();
        settlement.makeOffer(Offer.builder().build(), MadeBy.DEFENDANT, null);
        settlement.reject(MadeBy.CLAIMANT, null);

        Claim claim = SampleClaim.getClaimWithFullAdmission().toBuilder()
            .claimantRespondedAt(LocalDateTime.now())
            .claimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation.builder()
                .buildAcceptationIssueSettlementWithDefendantPaymentIntention())
            .settlement(settlement)
            .build();

        postRequest(COUNTERSIGN_SETTLEMENT_URL, claim)
            .andExpect(status().isConflict());
    }

    private ResultActions postRequest(String url, Claim claim) throws Exception {
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

        MockHttpServletRequestBuilder requestBuilder = post(url, claim.getExternalId())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN);
        return webClient.perform(requestBuilder);
    }
}
