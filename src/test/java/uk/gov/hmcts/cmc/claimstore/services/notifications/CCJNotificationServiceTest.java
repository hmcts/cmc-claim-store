package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NOTIFICATION_FAILURE;

@RunWith(MockitoJUnitRunner.class)
public class CCJNotificationServiceTest extends BaseNotificationServiceTest {

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private AppInsights appInsights;

    private CCJNotificationService ccjNotificationService;

    @Before
    public void setup() {
        ccjNotificationService = new CCJNotificationService(
            new NotificationService(notificationClient, appInsights),
            properties
        );

        when(templates.getEmail()).thenReturn(emailTemplates);
        when(properties.getTemplates()).thenReturn(templates);
    }

    @Test
    public void notifyClaimantShouldCallNotify() throws Exception {
        when(emailTemplates.getClaimantCCJRequested()).thenReturn(CLAIMANT_CCJ_REQUESTED_TEMPLATE);

        Claim claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        ccjNotificationService.notifyClaimantForCCJRequest(claim);

        verify(notificationClient)
            .sendEmail(
                eq(CLAIMANT_CCJ_REQUESTED_TEMPLATE),
                eq(claim.getSubmitterEmail()),
                anyMap(),
                eq(NotificationReferenceBuilder.CCJRequested.referenceForClaimant(claim.getReferenceNumber()))
            );
    }

    @Test(expected = NotificationException.class)
    public void shouldThrowExceptionWhenNotificationFails() throws Exception {
        when(emailTemplates.getClaimantCCJRequested()).thenReturn(CLAIMANT_CCJ_REQUESTED_TEMPLATE);

        Claim claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        when(notificationClient
            .sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(new NotificationClientException("Some problem"));

        ccjNotificationService.notifyClaimantForCCJRequest(claim);

        verify(notificationClient).sendEmail(anyString(), anyString(), anyMap(), anyString());
        verify(appInsights)
            .trackEvent(eq(NOTIFICATION_FAILURE), eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void notifyDefendantsShouldCallNotifyWhenCCJByAdmission() throws Exception {
        when(emailTemplates.getResponseByClaimantEmailToDefendant())
            .thenReturn(RESPONSE_BY_CLAIMANT_EMAIL_TO_DEFENDANT);

        Claim claim = SampleClaim
            .builder()
            .withDefendantEmail(SampleClaim.DEFENDANT_EMAIL)
            .withResponse(SampleResponse.PartAdmission.builder().build())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        ccjNotificationService.notifyDefendantForCCJRequested(claim);

        verify(notificationClient)
            .sendEmail(
                eq(RESPONSE_BY_CLAIMANT_EMAIL_TO_DEFENDANT),
                eq(claim.getDefendantEmail()),
                anyMap(),
                eq(NotificationReferenceBuilder.CCJIssued.referenceForDefendant(claim.getReferenceNumber()))
            );
    }

    @Test(expected = NotificationException.class)
    public void shouldThrowExceptionWhenNotificationFailsCCJByAdmission() throws Exception {
        when(emailTemplates.getResponseByClaimantEmailToDefendant())
            .thenReturn(RESPONSE_BY_CLAIMANT_EMAIL_TO_DEFENDANT);

        Claim claim = SampleClaim
            .builder()
            .withDefendantEmail(SampleClaim.DEFENDANT_EMAIL)
            .withResponse(SampleResponse.PartAdmission.builder().build())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        when(notificationClient
            .sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(new NotificationClientException("Some problem"));

        ccjNotificationService.notifyDefendantForCCJRequested(claim);

        verify(notificationClient).sendEmail(anyString(), anyString(), anyMap(), anyString());
        verify(appInsights)
            .trackEvent(eq(NOTIFICATION_FAILURE), eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void remindClaimantForCCJShouldCallNotify() throws Exception {
        when(emailTemplates.getClaimantCCJReminder()).thenReturn(CLAIMANT_CCJ_REMINDER_TEMPLATE);

        Claim claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        ccjNotificationService.notifyClaimantAboutCCJReminder(claim);

        verify(notificationClient)
            .sendEmail(
                eq(CLAIMANT_CCJ_REMINDER_TEMPLATE),
                eq(claim.getSubmitterEmail()),
                anyMap(),
                eq(NotificationReferenceBuilder.CCJRequested.reminderForClaimant(claim.getReferenceNumber()))
            );
    }
}
