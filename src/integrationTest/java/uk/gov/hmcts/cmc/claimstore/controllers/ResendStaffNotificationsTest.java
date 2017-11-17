package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ResponseData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponseData;
import uk.gov.hmcts.cmc.email.EmailData;

import java.time.LocalDate;
import java.util.List;

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
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.documentManagementUploadResponse;

public class ResendStaffNotificationsTest extends BaseIntegrationTest {
    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};
    private static final Resource resource = new ByteArrayResource(PDF_BYTES);

    @Mock
    private ResponseEntity<Resource> responseEntity;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Before
    public void setup() {
        given(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .willReturn(PDF_BYTES);

        given(documentUploadClientApi.upload(anyString(), any(List.class)))
            .willReturn(documentManagementUploadResponse());

        given(documentMetadataDownloadApi.getDocumentMetadata(anyString(), anyString()))
            .willReturn(documentManagementUploadResponse().getEmbedded().getDocuments().get(0));

        given(documentDownloadClientApi.downloadBinary(anyString(), anyString())).willReturn(responseEntity);

        given(responseEntity.getBody()).willReturn(resource);
    }

    @Test
    public void shouldRespond404WhenClaimDoesNotExist() throws Exception {
        final String nonExistingClaimReference = "something";
        final String event = "claim-issue";

        makeRequest(nonExistingClaimReference, event)
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldRespond404WhenEventIsNotSupported() throws Exception {
        final String nonExistingEvent = "some-event";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        makeRequest(claim.getReferenceNumber(), nonExistingEvent)
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldRespond409AndNotProceedForClaimIssuedEventWhenClaimIsLinkedToDefendant() throws Exception {
        final String event = "claim-issued";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        claimRepository.linkDefendant(claim.getId(), "2");

        makeRequest(claim.getReferenceNumber(), event)
            .andExpect(status().isConflict());

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void shouldRespond200AndSendNotificationsForClaimIssuedEvent() throws Exception {
        final String event = "claim-issued";

        Claim claim = claimStore.saveClaim(SampleClaimData.submittedByClaimant());

        final GeneratePinResponse pinResponse = new GeneratePinResponse("pin-123", "333");
        given(userService.generatePin(anyString(), eq("ABC123"))).willReturn(pinResponse);
        given(userService.getUserDetails(anyString())).willReturn(SampleUserDetails.getDefault());

        makeRequest(claim.getReferenceNumber(), event)
            .andExpect(status().isOk());

        verify(emailService).sendEmail(eq("sender@example.com"), emailDataArgument.capture());

        EmailData emailData = emailDataArgument.getValue();
        assertThat(emailData.getTo()).isEqualTo("recipient@example.com");
        assertThat(emailData.getSubject()).isEqualTo("Claim " + claim.getReferenceNumber() + " issued");
        assertThat(emailData.getMessage()).isEqualTo("Please find attached claim.");
    }

    @Test
    public void shouldRespond409AndNotProceedForMoreTimeRequestedEventWhenMoreTimeNotRequested() throws Exception {
        final String event = "more-time-requested";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        makeRequest(claim.getReferenceNumber(), event)
            .andExpect(status().isConflict());

        verify(notificationClient, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    public void shouldRespond200AndSendNotificationsForMoreTimeRequestedEvent() throws Exception {
        final String event = "more-time-requested";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        claimRepository.requestMoreTime(claim.getId(), LocalDate.now());

        makeRequest(claim.getReferenceNumber(), event)
            .andExpect(status().isOk());

        verify(notificationClient).sendEmail(eq("staff-more-time-requested-template"), eq("recipient@example.com"),
            any(), eq("more-time-requested-notification-to-staff-" + claim.getReferenceNumber()));
    }

    @Test
    public void shouldRespond409AndNotProceedForResponseSubmittedEventWhenResponseNotSubmitted() throws Exception {
        final String event = "response-submitted";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());

        makeRequest(claim.getReferenceNumber(), event)
            .andExpect(status().isConflict());

        verify(emailService, never()).sendEmail(any(), any());
    }

    @Test
    public void shouldRespond200AndSendNotificationsForResponseSubmittedEvent() throws Exception {
        final String event = "response-submitted";

        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        claimStore.saveResponse(
            claim.getId(),
            SampleResponseData
                .builder()
                .withResponseType(ResponseData.ResponseType.OWE_ALL_PAID_ALL)
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
