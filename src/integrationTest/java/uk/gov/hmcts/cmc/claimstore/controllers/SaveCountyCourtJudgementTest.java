package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;

import java.time.LocalDate;
import java.util.UUID;

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

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
public class SaveCountyCourtJudgementTest extends BaseIntegrationTest {
    @MockBean
    private CCJStaffNotificationHandler ccjStaffNotificationHandler;

    @Captor
    private ArgumentCaptor<CountyCourtJudgmentEvent> countyCourtJudgementEventArgument;

    private Claim claim;

    @Before
    public void setUp() {
        claim = claimStore.saveClaim(SampleClaimData.builder()
            .withExternalId(UUID.randomUUID()).build(), SUBMITTER_ID, LocalDate.now());

        claimStore.updateResponseDeadline(claim.getExternalId());

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
    }

    @Test
    public void shouldSaveCountyCourtJudgementRequest() throws Exception {

        CountyCourtJudgment countyCourtJudgment
            = SampleCountyCourtJudgment.builder().paymentOption(PaymentOption.IMMEDIATELY).build();

        makeRequest(claim.getExternalId(), countyCourtJudgment).andExpect(status().isOk());

        Claim claimWithCCJRequest = claimStore.getClaimByExternalId(claim.getExternalId());

        assertThat(claimWithCCJRequest.getCountyCourtJudgmentRequestedAt()).isNotNull();
        assertThat(claimWithCCJRequest.getCountyCourtJudgmentIssuedAt().isPresent()).isFalse();
    }

    @Test
    public void shouldInvokeStaffActionsHandlerAfterSuccessfulSave() throws Exception {
        CountyCourtJudgment countyCourtJudgment
            = SampleCountyCourtJudgment.builder().paymentOption(PaymentOption.IMMEDIATELY).build();

        makeRequest(claim.getExternalId(), countyCourtJudgment)
            .andExpect(status().isOk());

        verify(ccjStaffNotificationHandler)
            .onDefaultJudgmentRequestSubmitted(countyCourtJudgementEventArgument.capture());

        Claim updatedClaim = claimRepository.getById(claim.getId()).orElseThrow(RuntimeException::new);
        assertThat(countyCourtJudgementEventArgument.getValue().getClaim()).isEqualTo(updatedClaim);
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOk() throws Exception {
        CountyCourtJudgment countyCourtJudgment
            = SampleCountyCourtJudgment.builder().paymentOption(PaymentOption.IMMEDIATELY).build();

        makeRequest(claim.getExternalId(), countyCourtJudgment)
            .andExpect(status().isOk());

        verify(notificationClient, times(1))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    private ResultActions makeRequest(String externalId, CountyCourtJudgment countyCourtJudgment) throws Exception {
        String path = "/claims/" + externalId + "/county-court-judgment";

        return webClient
            .perform(post(path)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMapper.toJson(countyCourtJudgment))
            );
    }
}
