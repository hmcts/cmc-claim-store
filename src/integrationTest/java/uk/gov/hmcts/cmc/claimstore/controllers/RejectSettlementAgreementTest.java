package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class RejectSettlementAgreementTest extends BaseIntegrationTest {

    private Claim claim;

    @Before
    public void setUp() {
        claim = claimStore.saveClaim(SampleClaimData.builder()
            .withExternalId(UUID.randomUUID()).build(), SUBMITTER_ID, LocalDate.now());


        UserDetails defendantDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withMail(DEFENDANT_EMAIL)
            .withRoles("letter-" + claim.getLetterHolderId())
            .build();

        UserDetails claimantDetails = SampleUserDetails.builder()
            .withUserId(SUBMITTER_ID)
            .build();

        when(userService.getUserDetails(BEARER_TOKEN)).thenReturn(defendantDetails);
        when(userService.getUserDetails(AUTHORISATION_TOKEN)).thenReturn(claimantDetails);
        given(userService.getUser(BEARER_TOKEN)).willReturn(new User(BEARER_TOKEN, defendantDetails));
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).willReturn(PDF_BYTES);
        caseRepository.linkDefendant(BEARER_TOKEN);
        caseRepository.saveClaimantResponse(claim,
            new SampleClaimantResponse.ClaimantResponseAcceptation()
                .buildAcceptationIssueSettlementWithClaimantPaymentIntention(),
                BEARER_TOKEN);

        Settlement settlement = new Settlement();
        settlement.makeOffer(new Offer("offer", LocalDate.now(), null), MadeBy.DEFENDANT);
        settlement.accept(MadeBy.CLAIMANT);
        caseRepository.updateSettlement(claim, settlement, BEARER_TOKEN, SUBMITTER_ID);
    }

    @Test
    public void shouldRejectSettlementAgreement() throws Exception {
        makeRequest(claim.getExternalId()).andExpect(status().isCreated());

        Claim claimWithSettlementAgreement = claimStore.getClaimByExternalId(claim.getExternalId());

        assertThat(claimWithSettlementAgreement.getSettlement().orElseThrow(AssertionError::new)
            .getLastStatement().getType()).isEqualTo(StatementType.REJECTION);
    }

    private ResultActions makeRequest(String externalId) throws Exception {
        String path = String.format("/claims/%s/settlement-agreement/reject", externalId);

        return webClient
            .perform(post(path)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
            );
    }
}
