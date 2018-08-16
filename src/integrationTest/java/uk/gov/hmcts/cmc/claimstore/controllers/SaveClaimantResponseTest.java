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
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.math.BigDecimal;
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
public class SaveClaimantResponseTest extends BaseIntegrationTest {

    private Claim claim;

    @Before
    public void setUp() {
        claim = claimStore.saveClaim(SampleClaimData.builder()
            .withExternalId(UUID.randomUUID()).build(), SUBMITTER_ID, LocalDate.now());

        UserDetails userDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withMail(DEFENDANT_EMAIL)
            .withRoles("letter-" + claim.getLetterHolderId())
            .build();

        when(userService.getUserDetails(BEARER_TOKEN)).thenReturn(userDetails);
        given(userService.getUser(BEARER_TOKEN)).willReturn(new User(BEARER_TOKEN, userDetails));
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).willReturn(PDF_BYTES);
        caseRepository.linkDefendant(BEARER_TOKEN);

        claimStore.saveResponse(claim, SampleResponse.PartAdmission.builder().build());
    }

    @Test
    public void shouldSaveClaimantResponseAcceptation() throws Exception {
        ClaimantResponse response = SampleClaimantResponse.validDefaultAcceptation();

        makeRequest(claim.getExternalId(), SUBMITTER_ID, response)
            .andExpect(status().isCreated());

        Claim claimWithClaimantResponse = claimStore.getClaimByExternalId(claim.getExternalId());

        assertThat(claimWithClaimantResponse.getClaimantRespondedAt().isPresent()).isTrue();

        ResponseAcceptation claimantResponse = (ResponseAcceptation) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(AssertionError::new);

        assertThat(claimantResponse.getAmountPaid()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void shouldSaveClaimantResponseRejection() throws Exception {
        ClaimantResponse response = SampleClaimantResponse.validDefaultRejection();

        makeRequest(claim.getExternalId(), SUBMITTER_ID, response)
            .andExpect(status().isCreated());

        Claim claimWithClaimantResponse = claimStore.getClaimByExternalId(claim.getExternalId());

        assertThat(claimWithClaimantResponse.getClaimantRespondedAt().isPresent()).isTrue();

        ResponseRejection claimantResponse = (ResponseRejection) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(AssertionError::new);

        assertThat(claimantResponse.getReason())
            .isEqualTo("He paid 10 but he actually owes 10,000. No I do not accept this.");
        assertThat(claimantResponse.isFreeMediation()).isFalse();
        assertThat(claimantResponse.getAmountPaid()).isEqualTo(BigDecimal.TEN);
    }

    private ResultActions makeRequest(
        String externalId,
        String claimantId,
        ClaimantResponse response
    ) throws Exception {
        return webClient
            .perform(post("/responses/" + externalId + "/claimant/" + claimantId)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                .content(jsonMapper.toJson(response))
            );
    }
}
