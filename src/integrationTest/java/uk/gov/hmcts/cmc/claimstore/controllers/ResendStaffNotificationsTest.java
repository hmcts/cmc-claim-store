package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.cmc.claimstore.BaseTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleResponseData;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.ClaimData;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.email.EmailData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.SUBMITTER_EMAIL;

public class ResendStaffNotificationsTest extends BaseTest {

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Before
    public void setup() {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(new byte[]{1, 2, 3, 4});
    }

    @Test
    public void shouldRespond404WhenClaimDoesNotExist() throws Exception {
        final String nonExistingClaimReference = "something";
        final String event = "claim-issue";

        given(claimRepository.getByClaimReferenceNumber(nonExistingClaimReference)).willReturn(Optional.empty());

        webClient
            .perform(requestFor(nonExistingClaimReference, event))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldRespond404WhenEventIsNotSupported() throws Exception {
        final String claimReference = "000MC001";
        final String nonExistingEvent = "some-event";

        given(claimRepository.getByClaimReferenceNumber(claimReference)).willReturn(Optional.of(sampleClaim().build()));

        webClient
            .perform(requestFor(claimReference, nonExistingEvent))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    public void shouldRespond409AndNotProceedForClaimIssuedEventWhenClaimIsLinkedToDefendant() throws Exception {
        final String claimReference = "000MC001";
        final String event = "claim-issued";

        final Claim claim = sampleClaim().setDefendantId(DEFENDANT_ID).build();
        given(claimRepository.getByClaimReferenceNumber(claimReference)).willReturn(Optional.of(claim));

        webClient
            .perform(requestFor(claimReference, event))
            .andExpect(status().isConflict())
            .andReturn();

        verify(claimRepository, never()).linkLetterHolder(any(), any());
        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void shouldRespond200AndSendNotificationsForClaimIssuedEvent() throws Exception {
        final String claimReference = "000MC001";
        final String event = "claim-issued";

        final Claim claim = sampleClaim(SampleClaimData.submittedByClaimant()).setDefendantId(null).build();
        given(claimRepository.getByClaimReferenceNumber(claimReference)).willReturn(Optional.of(claim));

        final GeneratePinResponse pinResponse = new GeneratePinResponse("pin-123", 333L);
        given(userService.generatePin(anyString(), eq("ABC123"))).willReturn(pinResponse);
        given(userService.getUserDetails(anyString())).willReturn(SampleUserDetails.getDefault());

        webClient
            .perform(requestFor(claimReference, event))
            .andExpect(status().isOk())
            .andReturn();

        verify(emailService).sendEmail(eq("sender@example.com"), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue().getTo()).isEqualTo("recipient@example.com");
        assertThat(emailDataArgument.getValue().getSubject()).isEqualTo("Claim " + claimReference + " issued");
        assertThat(emailDataArgument.getValue().getMessage()).isEqualTo("Please find attached claim.");
    }

    @Test
    public void shouldRespond409AndNotProceedForMoreTimeRequestedEventWhenMoreTimeNotRequested() throws Exception {
        final String claimReference = "000MC001";
        final String event = "more-time-requested";

        final Claim claim = sampleClaim().setMoreTimeRequested(false).build();
        given(claimRepository.getByClaimReferenceNumber(claimReference)).willReturn(Optional.of(claim));

        webClient
            .perform(requestFor(claimReference, event))
            .andExpect(status().isConflict())
            .andReturn();

        verify(notificationClient, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    public void shouldRespond200AndSendNotificationsForMoreTimeRequestedEvent() throws Exception {
        final String claimReference = "000MC001";
        final String event = "more-time-requested";

        final Claim claim = sampleClaim().setMoreTimeRequested(true).build();
        given(claimRepository.getByClaimReferenceNumber(claimReference)).willReturn(Optional.of(claim));

        webClient
            .perform(requestFor(claimReference, event))
            .andExpect(status().isOk())
            .andReturn();

        verify(notificationClient).sendEmail(eq("staff-more-time-requested-template"),
            eq("recipient@example.com"), any(),
            eq("more-time-requested-notification-to-staff-" + claimReference));
    }

    @Test
    public void shouldRespond409AndNotProceedForResponseSubmittedEventWhenResponseNotSubmitted() throws Exception {
        final String claimReference = "000MC001";
        final String event = "response-submitted";

        final Claim claim = sampleClaim().setRespondedAt(LocalDateTime.now()).build();
        given(claimRepository.getByClaimReferenceNumber(claimReference)).willReturn(Optional.of(claim));

        webClient
            .perform(requestFor(claimReference, event))
            .andExpect(status().isConflict())
            .andReturn();

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void shouldRespond200AndSendNotificationsForResponseSubmittedEvent() throws Exception {
        final String claimReference = "000MC001";
        final String event = "response-submitted";

        final Claim claim = sampleClaim().setDefendantEmail("j.smith@example.com")
            .setResponse(SampleResponseData.validDefaults()).setRespondedAt(LocalDateTime.now()).build();

        given(claimRepository.getByClaimReferenceNumber(claimReference)).willReturn(Optional.of(claim));

        webClient
            .perform(requestFor(claimReference, event))
            .andExpect(status().isOk())
            .andReturn();

        verify(emailService).sendEmail(eq("sender@example.com"), emailDataArgument.capture());

        assertThat(emailDataArgument.getValue().getTo()).isEqualTo("recipient@example.com");
        assertThat(emailDataArgument.getValue().getSubject())
            .isEqualTo("Civil Money Claim defence submitted: John Rambo v John Smith " + claimReference);
        assertThat(emailDataArgument.getValue().getMessage()).contains(
            "The defendant has submitted an already paid defence which is attached as a PDF",
            "Email: j.smith@example.com",
            "Mobile number: 07873727165"
        );
    }

    private MockHttpServletRequestBuilder requestFor(String claimReference, String event) {
        return put("/support/claim/" + claimReference + "/event/" + event + "/resend-staff-notifications")
            .header(HttpHeaders.AUTHORIZATION, "ABC123");
    }

    private Claim.Builder sampleClaim() {
        return sampleClaim(null);
    }

    private Claim.Builder sampleClaim(final ClaimData claimData) {
        return new Claim.Builder()
            .setId(CLAIM_ID)
            .setExternalId(null)
            .setSubmitterId(SUBMITTER_ID)
            .setSubmitterEmail(SUBMITTER_EMAIL)
            .setLetterHolderId(LETTER_HOLDER_ID)
            .setDefendantId(DEFENDANT_ID)
            .setReferenceNumber(REFERENCE_NUMBER)
            .setClaimData(Optional.ofNullable(claimData).orElse(SampleClaimData.validDefaults()))
            .setIssuedOn(LocalDate.now())
            .setCreatedAt(LocalDateTime.now())
            .setResponseDeadline(LocalDate.now())
            .setMoreTimeRequested(false)
            .setRespondedAt(null);
    }
}
