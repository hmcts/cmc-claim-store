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
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(
    properties = {
        "document_management.url=false",
        "core_case_data.api.url=false"
    }
)
public class ResendStaffNotificationsTest extends BaseIntegrationTest {

    @MockBean
    protected SendLetterApi sendLetterApi;

    @MockBean
    protected DocumentManagementService documentManagementService;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Before
    public void setup() {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(new byte[]{1, 2, 3, 4});

        given(userService.getUserDetails(anyString())).willReturn(SampleUserDetails.getDefault());
    }

    @Test
    public void shouldRespond404WhenClaimDoesNotExist() throws Exception {
        String nonExistingClaimReference = "something";
        String event = "claim-issue";

        makeRequest(nonExistingClaimReference, event)
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldRespond404WhenEventIsNotSupported() throws Exception {
        String nonExistingEvent = "some-event";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        makeRequest(claim.getReferenceNumber(), nonExistingEvent)
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldRespond409AndNotProceedForClaimIssuedEventWhenClaimIsLinkedToDefendant() throws Exception {
        String event = "claim-issued";
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        UserDetails userDetails = SampleUserDetails.builder()
            .withUserId(DEFENDANT_ID)
            .withMail("defendant@example.com")
            .withRoles("letter-" + claim.getLetterHolderId())
            .build();

        given(userService.getUser(BEARER_TOKEN)).willReturn(new User(BEARER_TOKEN, userDetails));
        caseRepository.linkDefendant(BEARER_TOKEN);

        makeRequest(claim.getReferenceNumber(), event)
            .andExpect(status().isConflict());

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void shouldRespond200AndSendNotificationsForClaimIssuedEvent() throws Exception {
        String event = "claim-issued";

        Claim claim = claimStore.saveClaim(SampleClaimData.submittedByClaimant());

        GeneratePinResponse pinResponse = new GeneratePinResponse("pin-123", "333");
        given(userService.generatePin(anyString(), eq("ABC123"))).willReturn(pinResponse);
        given(sendLetterApi.sendLetter(any(), any())).willReturn(new SendLetterResponse(UUID.randomUUID()));

        makeRequest(claim.getReferenceNumber(), event)
            .andExpect(status().isOk());

        verify(emailService, atLeast(2)).sendEmail(eq("sender@example.com"), emailDataArgument.capture());

        EmailData emailData = emailDataArgument.getValue();
        assertThat(emailData.getTo()).isEqualTo("recipient@example.com");
        assertThat(emailData.getSubject()).isEqualTo("Claim " + claim.getReferenceNumber() + " issued");
        assertThat(emailData.getMessage()).isEqualTo("Please find attached claim.");
    }

    @Test
    public void shouldRespond409AndNotProceedForMoreTimeRequestedEventWhenMoreTimeNotRequested() throws Exception {
        String event = "more-time-requested";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        makeRequest(claim.getReferenceNumber(), event)
            .andExpect(status().isConflict());

        verify(notificationClient, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    public void shouldRespond200AndSendNotificationsForMoreTimeRequestedEvent() throws Exception {
        String event = "more-time-requested";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        claimRepository.requestMoreTime(claim.getExternalId(), LocalDate.now());

        makeRequest(claim.getReferenceNumber(), event)
            .andExpect(status().isOk());

        verify(notificationClient).sendEmail(eq("staff-more-time-requested-template"), eq("recipient@example.com"),
            any(), eq("more-time-requested-notification-to-staff-" + claim.getReferenceNumber()));
    }

    @Test
    public void shouldRespond409AndNotProceedForResponseSubmittedEventWhenResponseNotSubmitted() throws Exception {
        String event = "response-submitted";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        makeRequest(claim.getReferenceNumber(), event)
            .andExpect(status().isConflict());

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void shouldRespond200AndSendNotificationsForResponseSubmittedEvent() throws Exception {
        String event = "response-submitted";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        claimStore.saveResponse(
            claim,
            SampleResponse.FullDefence
                .builder()
                .withDefenceType(DefenceType.ALREADY_PAID)
                .withMediation(null)
                .build(),
            DEFENDANT_ID,
            "j.smith@example.com"
        );

        makeRequest(claim.getReferenceNumber(), event)
            .andExpect(status().isOk());

        verify(emailService).sendEmail(eq("sender@example.com"), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue().getTo()).isEqualTo("recipient@example.com");
        assertThat(emailDataArgument.getValue().getSubject())
            .isEqualTo("Civil Money Claim defence submitted: John Rambo v John Smith " + claim.getReferenceNumber());
        assertThat(emailDataArgument.getValue().getMessage()).contains(
            "The defendant has submitted an already paid defence which is attached as a PDF",
            "Email: j.smith@example.com",
            "Mobile number: 07873727165"
        );
    }

    private ResultActions makeRequest(String referenceNumber, String event) throws Exception {
        return webClient
            .perform(put("/support/claim/" + referenceNumber + "/event/" + event + "/resend-staff-notifications")
                .header(HttpHeaders.AUTHORIZATION, "ABC123"));
    }
}
