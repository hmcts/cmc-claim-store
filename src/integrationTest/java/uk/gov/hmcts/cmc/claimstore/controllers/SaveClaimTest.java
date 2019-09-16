package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableList;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.cmc.claimstore.MockedCoreCaseDataApiTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleEvidence;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTimeline;
import uk.gov.hmcts.cmc.email.EmailAttachment;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
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
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NOTIFICATION_FAILURE;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@TestPropertySource(
    properties = {
        "feature_toggles.async_event_operations_enabled=false"
    }
)
public class SaveClaimTest extends MockedCoreCaseDataApiTest {

    private static final String REPRESENTATIVE_EMAIL_TEMPLATE = "f2b21b9c-fc4a-4589-807b-3156dbf5bf01";

    @MockBean
    protected SendLetterApi sendLetterApi;

    @Captor
    private ArgumentCaptor<EmailData> emailDataArgument;

    @Ignore
    @Test
    public void shouldReturnNewlyCreatedClaim() throws Exception {
        ClaimData citizenClaimData = SampleClaimData.submittedByClaimant();
        Long citizenCaseId = citizenSampleCaseDetails.getId();

        MvcResult result = makeSuccessfulIssueClaimRequestForCitizen(citizenClaimData, String.valueOf(citizenCaseId));

        //assertThat(deserializeObjectFrom(result, Claim.class).getId()).isEqualTo(citizenCaseId);
        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getClaimData)
            .isEqualTo(citizenClaimData);
    }

    @Test
    public void shouldReturnFeaturesStored() throws Exception {
        ClaimData citizenClaimData = SampleClaimData.submittedByClaimant();
        Long citizenCaseId = citizenSampleCaseDetails.getId();
        String externalId = citizenClaimData.getExternalId().toString();

        ImmutableList<String> features = ImmutableList.of("admissions", "offers");

        commonStubStepsClaimRequestForCitizen(String.valueOf(citizenCaseId), externalId);

        MvcResult result = webClient
            .perform(MockMvcRequestBuilders.post("/claims/" + USER_ID)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .header("Features", features)
                .content(jsonMapper.toJson(citizenClaimData))
            )
            .andExpect(status().isOk())
            .andReturn();

        assertThat(deserializeObjectFrom(result, Claim.class))
            .extracting(Claim::getFeatures)
            .isEqualTo(features);
    }

    @Test
    public void shouldFailWhenStaffNotificationFails() throws Exception {
        ClaimData citizenClaimData = SampleClaimData.submittedByClaimant();
        String externalId = citizenClaimData.getExternalId().toString();

        doThrow(new RuntimeException("Sending failed"))
            .when(emailService).sendEmail(anyString(), any(EmailData.class));

        stubForSearchNonExistingClaimForCitizen(externalId);
        stubForStartForCitizenWithServerError();

        makeIssueClaimRequest(citizenClaimData, AUTHORISATION_TOKEN)
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void shouldRetrySendNotifications() throws Exception {
        ClaimData citizenClaimData = SampleClaimData.submittedByClaimant();
        Long citizenCaseId = citizenSampleCaseDetails.getId();
        String externalId = citizenClaimData.getExternalId().toString();

        given(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email1")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email2")))
            .willThrow(new NotificationClientException(new RuntimeException("invalid email3")));

        commonStubStepsClaimRequestForCitizen(String.valueOf(citizenCaseId), externalId);
        MvcResult result = makeIssueClaimRequest(citizenClaimData, AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        Claim claim = deserializeObjectFrom(result, Claim.class);

        verify(notificationClient, atLeast(3))
            .sendEmail(anyString(), anyString(), anyMap(), anyString());

        verify(appInsights).trackEvent(
            eq(NOTIFICATION_FAILURE),
            eq(REFERENCE_NUMBER),
            eq("claimant-issue-notification-" + claim.getReferenceNumber())
        );
    }

    @Test
    public void shouldSendNotificationsWhenEverythingIsOk() throws Exception {
        ClaimData legalRepresentativeClaimData = SampleClaimData.submittedByLegalRepresentative();
        String legalRepresentativeCaseId = representativeSampleCaseDetails.getId().toString();
        String externalId = legalRepresentativeClaimData.getExternalId().toString();

        given(notificationClient.sendEmail(any(), any(), any(), any())).willReturn(null);

        commonStubStepsClaimRequestForRepresentative(legalRepresentativeCaseId, externalId);

        MvcResult result = makeIssueClaimRequest(legalRepresentativeClaimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        Claim savedClaim = deserializeObjectFrom(result, Claim.class);

        verify(notificationClient).sendEmail(eq(REPRESENTATIVE_EMAIL_TEMPLATE), anyString(),
            anyMap(), eq("representative-issue-notification-" + savedClaim.getReferenceNumber()));
    }

    @Test
    public void shouldSendStaffNotificationsForCitizenClaimIssuedEvent() throws Exception {
        ClaimData citizenClaimData = SampleClaimData.submittedByClaimant();
        Long citizenCaseId = citizenSampleCaseDetails.getId();
        String externalId = citizenClaimData.getExternalId().toString();

        commonStubStepsClaimRequestForCitizen(String.valueOf(citizenCaseId), externalId);

        MvcResult result = makeIssueClaimRequest(citizenClaimData, AUTHORISATION_TOKEN)
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
        ClaimData legalRepresentativeClaimData = SampleClaimData.submittedByLegalRepresentative();
        String legalRepresentativeCaseId = representativeSampleCaseDetails.getId().toString();
        String externalId = legalRepresentativeClaimData.getExternalId().toString();

        commonStubStepsClaimRequestForRepresentative(legalRepresentativeCaseId, externalId);

        MvcResult result = makeIssueClaimRequest(legalRepresentativeClaimData, SOLICITOR_AUTHORISATION_TOKEN)
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

}
