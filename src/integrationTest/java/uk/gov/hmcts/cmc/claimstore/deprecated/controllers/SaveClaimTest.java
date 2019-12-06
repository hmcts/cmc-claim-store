package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.cmc.claimstore.deprecated.BaseSaveTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleEvidence;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTimeline;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.service.notify.NotificationClientException;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_ISSUED_CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=false"
    }
)
@ActiveProfiles("test")
public class SaveClaimTest extends BaseSaveTest {

    private static final String REPRESENTATIVE_EMAIL_TEMPLATE = "f2b21b9c-fc4a-4589-807b-3156dbf5bf01";

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Test
    public void shouldReturnNewlyCreatedClaim() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimant();

        MvcResult result = makeIssueClaimRequest(claimData, AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .isEqualTo(claimData);

        Claim updated = postClaimOperation.retrieveClaim(claimData.getExternalId().toString(), AUTHORISATION_TOKEN);

        assertThat(updated.getClaimSubmissionOperationIndicators())
            .isEqualTo(ClaimSubmissionOperationIndicators.builder()
                .bulkPrint(YES)
                .claimantNotification(YES)
                .defendantNotification(YES)
                .rpa(YES)
                .sealedClaimUpload(YES)
                .claimIssueReceiptUpload(YES)
                .staffNotification(YES)
                .build()
            );
    }

    @Test
    public void shouldReturnFeaturesStored() throws Exception {
        ClaimData claimData = SampleClaimData.submittedByClaimant();

        ImmutableList<String> features = ImmutableList.of("admissions", "offers");

        MvcResult result = webClient
            .perform(MockMvcRequestBuilders.post("/claims/" + USER_ID)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .header("Features", features)
                .content(jsonMapper.toJson(claimData))
            )
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getFeatures)
            .isEqualTo(features);
    }

    @Test
    public void shouldFailWhenStaffNotificationFails() throws Exception {
        doThrow(new RuntimeException("Sending failed"))
            .when(emailService).sendEmail(anyString(), any(EmailData.class));

        MvcResult result = makeIssueClaimRequest(SampleClaimData.submittedByClaimant(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        Claim claim = deserializeObjectFrom(result, Claim.class);

        Claim updated = postClaimOperation.retrieveClaim(claim.getExternalId(), AUTHORISATION_TOKEN);

        assertThat(updated.getClaimSubmissionOperationIndicators().getClaimantNotification()).isEqualTo(YesNoOption.NO);
    }

    @Test
    public void shouldRetrySendNotifications() throws Exception {
        given(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email1")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email2")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email3")));

        ClaimData claimData = SampleClaimData.submittedByClaimant();

        MvcResult result = makeIssueClaimRequest(claimData, AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        Claim claim = deserializeObjectFrom(result, Claim.class);

        postClaimOperation.retrieveClaim(claim.getExternalId(), AUTHORISATION_TOKEN);

        verify(notificationClient, atLeast(3))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());

        verify(appInsights).trackEvent(
            eq(CLAIM_ISSUED_CITIZEN),
            eq(REFERENCE_NUMBER),
            eq(claim.getReferenceNumber())
        );
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOk() throws Exception {
        given(notificationClient.sendEmail(any(), any(), any(), any())).willReturn(null);

        MvcResult result = makeIssueClaimRequest(SampleClaimData.submittedByLegalRepresentative(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        Claim savedClaim = deserializeObjectFrom(result, Claim.class);

        verify(notificationClient).sendEmail(eq(REPRESENTATIVE_EMAIL_TEMPLATE), anyString(),
            anyMap(), eq("representative-issue-notification-" + savedClaim.getReferenceNumber()));
    }

    @Test
    public void shouldSendStaffNotificationsForCitizenClaimIssuedEvent() throws Exception {
        MvcResult result = makeIssueClaimRequest(SampleClaimData.submittedByClaimant(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        Claim savedClaim = deserializeObjectFrom(result, Claim.class);

        postClaimOperation.retrieveClaim(savedClaim.getExternalId(), AUTHORISATION_TOKEN);

        verify(emailService, atLeast(2))
            .sendEmail(eq("sender@example.com"), emailDataArgument.capture());

        EmailData emailData = emailDataArgument.getValue();
        assertThat(emailData.getTo()).isEqualTo("recipient@example.com");
        assertThat(emailData.getSubject()).isEqualTo("J new claim " + savedClaim.getReferenceNumber());
        assertThat(emailData.getMessage()).contains("Please find attached claim.");
        assertThat(emailData.getAttachments()).hasSize(2)
            .extracting(EmailAttachment::getFilename)
            .containsExactly(savedClaim.getReferenceNumber() + "-claim-form.pdf",
                savedClaim.getReferenceNumber() + "-json-claim.json");
    }

    @Test
    public void shouldSendStaffNotificationsForLegalClaimIssuedEvent() throws Exception {
        MvcResult result = makeIssueClaimRequest(SampleClaimData.submittedByLegalRepresentative(), AUTHORISATION_TOKEN)
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
            .isEqualTo(savedClaim.getReferenceNumber() + "-claim-form.pdf");
    }

    @Test
    public void shouldNotMakeCallToStoreInCoreCaseDataStoreWhenToggledOff() throws Exception {
        makeIssueClaimRequest(SampleClaimData.submittedByLegalRepresentative(), AUTHORISATION_TOKEN)
            .andExpect(status().isOk());

        verify(coreCaseDataApi, never())
            .startForCaseworker(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenClaimWithInvalidTimelineIsSubmitted() throws Exception {
        ClaimData invalidClaimData = SampleClaimData.submittedByClaimantBuilder()
            .withTimeline(SampleTimeline.builder().withEvents(asList(new TimelineEvent[1001])).build())
            .build();

        makeIssueClaimRequest(invalidClaimData, AUTHORISATION_TOKEN)
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenClaimWithInvalidEvidenceIsSubmitted() throws Exception {
        ClaimData invalidClaimData = SampleClaimData.submittedByClaimantBuilder()
            .withEvidence(SampleEvidence.builder().withRows(asList(new EvidenceRow[1001])).build())
            .build();

        makeIssueClaimRequest(invalidClaimData, AUTHORISATION_TOKEN)
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenClaimAmountIsMissing() throws Exception {
        ClaimData invalidClaimData = SampleClaimData.submittedByClaimantBuilder().withAmount(null).build();

        makeIssueClaimRequest(invalidClaimData, AUTHORISATION_TOKEN)
            .andExpect(status().isUnprocessableEntity());
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
        makeIssueClaimRequest(claimData, AUTHORISATION_TOKEN)
            .andExpect(status().isOk());

        verify(documentUploadClient, never()).upload(any(), any(), any(), any());
    }
}
