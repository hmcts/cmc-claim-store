package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.service.notify.NotificationClientException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@TestPropertySource(
    properties = {
        "document_management.api_gateway.url=false",
        "core_case_data.api.url=false"
    }
)
public class SaveClaimTest extends BaseSaveTest {

    private static final String REPRESENTATIVE_EMAIL_TEMPLATE = "f2b21b9c-fc4a-4589-807b-3156dbf5bf01";

    @MockBean
    protected SendLetterApi sendLetterApi;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Test
    public void shouldReturnNewlyCreatedClaim() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimant();

        MvcResult result = makeRequest(claimData)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .contains(claimData);
    }

    @Test
    public void shouldFailWhenDuplicateExternalId() throws Exception {
        UUID externalId = UUID.randomUUID();

        ClaimData claimData = SampleClaimData.builder().withExternalId(externalId).build();
        claimStore.saveClaim(claimData);

        makeRequest(claimData)
            .andExpect(status().isConflict());
    }

    @Test
    public void shouldFailWhenStaffNotificationFails() throws Exception {
        doThrow(new RuntimeException("Sending failed"))
            .when(emailService).sendEmail(anyString(), any(EmailData.class));

        makeRequest(SampleClaimData.submittedByClaimant())
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void shouldRetrySendNotifications() throws Exception {
        given(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email1")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email2")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email3")));

        makeRequest(SampleClaimData.submittedByClaimant())
            .andExpect(status().isOk());

        verify(notificationClient, atLeast(3))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOk() throws Exception {
        given(notificationClient.sendEmail(any(), any(), any(), any())).willReturn(null);

        MvcResult result = makeRequest(SampleClaimData.submittedByLegalRepresentative())
            .andExpect(status().isOk())
            .andReturn();

        Claim savedClaim = deserializeObjectFrom(result, Claim.class);

        verify(notificationClient).sendEmail(eq(REPRESENTATIVE_EMAIL_TEMPLATE), anyString(),
            anyMap(), eq("representative-issue-notification-" + savedClaim.getReferenceNumber()));
    }

    @Test
    public void shouldSendStaffNotificationsForCitizenClaimIssuedEvent() throws Exception {
        MvcResult result = makeRequest(SampleClaimData.submittedByClaimant())
            .andExpect(status().isOk())
            .andReturn();

        Claim savedClaim = deserializeObjectFrom(result, Claim.class);

        verify(emailService, atLeast(2))
            .sendEmail(eq("sender@example.com"), emailDataArgument.capture());

        EmailData emailData = emailDataArgument.getValue();
        assertThat(emailData.getTo()).isEqualTo("recipient@example.com");
        assertThat(emailData.getSubject()).isEqualTo("Claim " + savedClaim.getReferenceNumber() + " issued");
        assertThat(emailData.getMessage()).isEqualTo("Please find attached claim.");
        assertThat(emailData.getAttachments()).hasSize(2)
            .extracting(EmailAttachment::getFilename)
            .containsExactly(savedClaim.getReferenceNumber() + "-claim-form.pdf",
                savedClaim.getReferenceNumber() + "-defendant-pin-letter.pdf");
    }

    @Test
    public void shouldSendStaffNotificationsForLegalClaimIssuedEvent() throws Exception {
        MvcResult result = makeRequest(SampleClaimData.submittedByLegalRepresentative())
            .andExpect(status().isOk())
            .andReturn();

        Claim savedClaim = deserializeObjectFrom(result, Claim.class);

        verify(emailService, once())
            .sendEmail(eq("sender@example.com"), emailDataArgument.capture());

        EmailData emailData = emailDataArgument.getValue();
        assertThat(emailData.getTo()).isEqualTo("recipient@example.com");
        assertThat(emailData.getSubject()).isEqualTo("Claim form " + savedClaim.getReferenceNumber());
        assertThat(emailData.getMessage()).isEqualTo("Please find attached claim.");
        assertThat(emailData.getAttachments()).hasSize(1)
            .first().extracting(EmailAttachment::getFilename)
            .containsExactly(savedClaim.getReferenceNumber() + "-claim-form.pdf");
    }

    @Test
    public void shouldNotMakeCallToStoreInCoreCaseDataStoreWhenToggledOff() throws Exception {
        makeRequest(SampleClaimData.submittedByLegalRepresentative())
            .andExpect(status().isOk());

        verify(coreCaseDataApi, never())
            .startForCaseworker(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void shouldNotUploadSealedCopyOfNonRepresentedClaimIntoDocumentManagementStore() throws Exception {
        assertSealedClaimIsNotUploadedToDocumentManagementStore(SampleClaimData.submittedByClaimant());
    }

    @Test
    public void shouldNotUploadSealedCopyOfRepresentedClaimIntoDocumentManagementStore() throws Exception {
        assertSealedClaimIsNotUploadedToDocumentManagementStore(SampleClaimData.submittedByLegalRepresentative());
    }

    private void assertSealedClaimIsNotUploadedToDocumentManagementStore(ClaimData claimData) throws Exception {
        makeRequest(claimData)
            .andExpect(status().isOk());

        verify(documentUploadClient, never()).upload(anyString(), anyList());
    }
}
