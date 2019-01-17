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
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleDefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleDefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIMANT_EMAIL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse.ClaimantResponseAcceptation.builder;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class SaveClaimantResponseTest extends BaseIntegrationTest {

    private Claim claim;

    @Before
    public void setUp() {
        claim = claimStore.saveClaim(SampleClaimData.submittedByClaimant(), SUBMITTER_ID, LocalDate.now());

        UserDetails userDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withMail(DEFENDANT_EMAIL)
            .withRoles("letter-" + claim.getLetterHolderId())
            .build();

        UserDetails claimantDetails = SampleUserDetails.builder()
            .withUserId(SUBMITTER_ID)
            .withMail(CLAIMANT_EMAIL)
            .build();

        when(userService.getUserDetails(BEARER_TOKEN)).thenReturn(userDetails);
        when(userService.getUserDetails(AUTHORISATION_TOKEN)).thenReturn(claimantDetails);
        given(userService.getUser(BEARER_TOKEN)).willReturn(new User(BEARER_TOKEN, userDetails));
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(new User(AUTHORISATION_TOKEN, claimantDetails));
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).willReturn(PDF_BYTES);
        caseRepository.linkDefendant(BEARER_TOKEN);

        claimStore.saveResponse(
            claim,
            SampleResponse.PartAdmission.builder().buildWithPaymentOptionImmediately()
        );
    }

    @Test
    public void shouldSaveClaimantResponseAcceptation() throws Exception {
        ClaimantResponse response = builder().buildAcceptationIssueCCJWithCourtDetermination();

        makeRequest(claim.getExternalId(), SUBMITTER_ID, response)
            .andExpect(status().isCreated());

        Claim claimWithClaimantResponse = claimStore.getClaimByExternalId(claim.getExternalId());

        assertThat(claimWithClaimantResponse.getClaimantRespondedAt().isPresent()).isTrue();

        ResponseAcceptation claimantResponse = (ResponseAcceptation) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(AssertionError::new);

        assertThat(claimantResponse.getAmountPaid().orElse(null)).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void shouldSaveClaimantResponseAcceptationReferToJudge() throws Exception {
        ClaimantResponse response = builder().buildAcceptationReferToJudgeWithCourtDetermination();

        makeRequest(claim.getExternalId(), SUBMITTER_ID, response)
            .andExpect(status().isCreated());

        Claim claimWithClaimantResponse = claimStore.getClaimByExternalId(claim.getExternalId());

        assertThat(claimWithClaimantResponse.getClaimantRespondedAt().isPresent()).isTrue();

        ResponseAcceptation claimantResponse = (ResponseAcceptation) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(AssertionError::new);

        assertThat(claimantResponse.getFormaliseOption().equals(FormaliseOption.REFER_TO_JUDGE));
        assertThat(claimantResponse.getCourtDetermination()).isPresent();
        assertThat(claimantResponse.getClaimantPaymentIntention()).isPresent();

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

        assertThat(claimantResponse.getFreeMediation()).isNotEmpty();
        assertThat(claimantResponse.getAmountPaid().orElse(null)).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void shouldSaveClaimantResponseAcceptationWithoutFormaliseOptionOnStatePaid() throws Exception {
        ClaimantResponse response = builder().buildStatePaidAcceptationWithoutFormaliseOption();

        makeRequest(claim.getExternalId(), SUBMITTER_ID, response)
            .andExpect(status().isCreated());

        Claim claimWithClaimantResponse = claimStore.getClaimByExternalId(claim.getExternalId());

        assertThat(claimWithClaimantResponse.getClaimantResponse()).isPresent();

        ClaimantResponse claimantResponse = claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(AssertionError::new);

        assertThat(claimantResponse.getType()).isEqualTo(ClaimantResponseType.ACCEPTATION);
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOkForAcceptation() throws Exception {

        claim = claimStore.saveClaim(SampleClaimData.submittedByClaimant(), SUBMITTER_ID, LocalDate.now());

        UserDetails userDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withMail(DEFENDANT_EMAIL)
            .withRoles("letter-" + claim.getLetterHolderId())
            .build();

        UserDetails claimantDetails = SampleUserDetails.builder()
            .withUserId(SUBMITTER_ID)
            .withMail(CLAIMANT_EMAIL)
            .build();

        when(userService.getUserDetails(BEARER_TOKEN)).thenReturn(userDetails);
        when(userService.getUserDetails(AUTHORISATION_TOKEN)).thenReturn(claimantDetails);
        given(userService.getUser(BEARER_TOKEN)).willReturn(new User(BEARER_TOKEN, userDetails));
        given(userService.getUser(AUTHORISATION_TOKEN)).willReturn(new User(AUTHORISATION_TOKEN, claimantDetails));
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap())).willReturn(PDF_BYTES);
        caseRepository.linkDefendant(BEARER_TOKEN);

        Response defendantResponse = PartAdmissionResponse.builder()
            .defendant(SampleParty.builder().individual())
            .paymentIntention(
                PaymentIntention.builder().paymentOption(PaymentOption.IMMEDIATELY).build()
            )
            .moreTimeNeeded(YesNoOption.NO)
            .amount(BigDecimal.valueOf(120))
            .paymentDeclaration(null)
            .defence("defense")
            .timeline(SampleDefendantTimeline.validDefaults())
            .evidence(SampleDefendantEvidence.validDefaults())
            .build();

        claimStore.saveResponse(claim, defendantResponse);

        ClaimantResponse response = builder().buildAcceptationIssueCCJWithCourtDetermination();

        makeRequest(claim.getExternalId(), SUBMITTER_ID, response);

        verify(notificationClient, times(1))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOkForRejection() throws Exception {
        ClaimantResponse response = SampleClaimantResponse.validDefaultRejection();

        makeRequest(claim.getExternalId(), SUBMITTER_ID, response);

        verify(notificationClient, times(1))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    private ResultActions makeRequest(
        String externalId,
        String claimantId,
        ClaimantResponse response
    ) throws Exception {
        return webClient
            .perform(post("/responses/" + externalId + "/claimant/" + claimantId)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMapper.toJson(response))
            );
    }
}
