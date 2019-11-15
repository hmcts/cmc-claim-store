package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.claimstore.services.FreeMediationDecisionDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NOTIFICATION_FAILURE;
import static uk.gov.hmcts.cmc.claimstore.utils.DirectionsQuestionnaireUtils.DQ_FLAG;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseNotificationServiceTest extends BaseNotificationServiceTest {

    private final String reference = "defendant-response-notification-" + claim.getReferenceNumber();
    private DefendantResponseNotificationService service;

    @Before
    public void beforeEachTest() {
        service = new DefendantResponseNotificationService(
            new NotificationService(notificationClient, appInsights),
            new FreeMediationDecisionDateCalculator(28),
            properties
        );

        when(properties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(properties.getTemplates()).thenReturn(templates);
        when(emailTemplates.getDefendantResponseIssued()).thenReturn(DEFENDANT_RESPONSE_TEMPLATE);
        when(emailTemplates.getDefendantResponseWithNoMediationIssued())
            .thenReturn(DEFENDANT_RESPONSE_NO_MEDIATION_TEMPLATE);

        when(emailTemplates.getDefendantResponseForDqPilotWithNoMediationIssued())
            .thenReturn(ONLINE_DQ_WITH_NO_MEDIATION_DEFENDANT_RESPONSE_TEMPLATE);

        when(emailTemplates.getClaimantResponseForDqPilotWithNoMediationIssued())
            .thenReturn(ONLINE_DQ_WITH_NO_MEDIATION_CLAIMANT_RESPONSE_TEMPLATE);
    }

    @Test(expected = NotificationException.class)
    public void notifyDefendantShouldThrowRuntimeExceptionWhenNotificationClientThrows() throws Exception {
        when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(mock(NotificationClientException.class));

        service.notifyDefendant(claim, USER_EMAIL, reference);
        verify(appInsights).trackEvent(eq(NOTIFICATION_FAILURE), eq(REFERENCE_NUMBER), eq(reference));
    }

    @Test
    public void notifyDefendantShouldSendEmailUsingPredefinedTemplate() throws Exception {
        service.notifyDefendant(claim, USER_EMAIL, reference);

        verify(notificationClient).sendEmail(
            eq(DEFENDANT_RESPONSE_TEMPLATE), anyString(), anyMap(), anyString());
    }

    @Test
    public void notifyDefendantShouldSendToDefendantEmail() throws Exception {
        service.notifyDefendant(claim, USER_EMAIL, reference);

        verify(notificationClient).sendEmail(
            eq(DEFENDANT_RESPONSE_TEMPLATE), eq(USER_EMAIL), anyMap(), anyString());
    }

    @Test
    public void notifyDefendantShouldUseClaimReferenceNumberForNotificationReference() throws Exception {
        service.notifyDefendant(claim, USER_EMAIL, reference);

        verify(notificationClient)
            .sendEmail(eq(DEFENDANT_RESPONSE_TEMPLATE), eq(USER_EMAIL), anyMap(), eq(reference));
    }

    @Test
    public void notifyDefendantShouldUseDefendantResponseEmailTemplateFullDefenceDisputeNoMediation()
        throws Exception {
        service.notifyDefendant(SampleClaim.getClaimWithFullDefenceNoMediation(), USER_EMAIL, reference);

        verify(notificationClient)
            .sendEmail(eq(DEFENDANT_RESPONSE_NO_MEDIATION_TEMPLATE), eq(USER_EMAIL), anyMap(), eq(reference));
    }

    @Test
    public void shouldNotifyDefendantForFullDefenceWithOnlineDqAndNoMediation() throws Exception {
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation().toBuilder()
            .features(ImmutableList.of(DQ_FLAG, "admissions"))
            .build();

        service.notifyDefendant(claim, USER_EMAIL, reference);

        verify(notificationClient).sendEmail(
            eq(ONLINE_DQ_WITH_NO_MEDIATION_DEFENDANT_RESPONSE_TEMPLATE),
            eq(USER_EMAIL),
            anyMap(),
            eq(reference)
        );
    }

    @Test
    public void shouldNotifyClaimantForFullDefenceWithOnlineDqAndNoMediation() throws Exception {
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation().toBuilder()
            .features(ImmutableList.of(DQ_FLAG, "admissions"))
            .build();

        service.notifyClaimant(claim, reference);

        verify(notificationClient).sendEmail(
            eq(ONLINE_DQ_WITH_NO_MEDIATION_CLAIMANT_RESPONSE_TEMPLATE),
            eq(claim.getSubmitterEmail()),
            anyMap(),
            eq(reference)
        );
    }

    @Test
    public void shouldNotifyDefendantForPartAdmissionWithOnlineDqAndNoMediation() throws Exception {
        Claim claim = SampleClaim.getClaimWithPartAdmissionAndNoMediation().toBuilder()
            .features(ImmutableList.of(DQ_FLAG, "admissions"))
            .build();

        service.notifyDefendant(claim, USER_EMAIL, reference);

        verify(notificationClient).sendEmail(
            eq(ONLINE_DQ_WITH_NO_MEDIATION_DEFENDANT_RESPONSE_TEMPLATE),
            eq(USER_EMAIL),
            anyMap(),
            eq(reference)
        );
    }

    @Test
    public void shouldNotifyClaimantForPartAdmissionWithOnlineDqAndNoMediation() throws Exception {
        Claim claim = SampleClaim.getClaimWithPartAdmissionAndNoMediation().toBuilder()
            .features(ImmutableList.of(DQ_FLAG, "admissions"))
            .build();

        service.notifyClaimant(claim, reference);

        verify(notificationClient).sendEmail(
            eq(ONLINE_DQ_WITH_NO_MEDIATION_CLAIMANT_RESPONSE_TEMPLATE),
            eq(claim.getSubmitterEmail()),
            anyMap(),
            eq(reference)
        );
    }

    @Test
    public void notifyDefendantShouldUseDefendantResponseEmailTemplateFullDefenceDisputeYesMediation()
        throws Exception {
        Claim claim = SampleClaim.getDefault().toBuilder().respondedAt(LocalDateTime.now()).build();
        service.notifyDefendant(claim, USER_EMAIL, reference);

        verify(notificationClient)
            .sendEmail(eq(DEFENDANT_RESPONSE_TEMPLATE), eq(USER_EMAIL), anyMap(), eq(reference));
    }

    @Test
    public void notifyDefendantShouldUseDefendantResponseEmailTemplateFullDefenceAlreadyPaidFullAmount()
        throws Exception {
        service.notifyDefendant(SampleClaim.getClaimWithFullDefenceAlreadyPaid(), USER_EMAIL, reference);

        verify(notificationClient)
            .sendEmail(eq(DEFENDANT_RESPONSE_TEMPLATE), eq(USER_EMAIL), anyMap(), eq(reference));
    }

    @Test
    public void notifyDefendantShouldPassFrontendHostInTemplateParameters() throws Exception {
        service.notifyDefendant(claim, USER_EMAIL, reference);

        verify(notificationClient).sendEmail(
            eq(DEFENDANT_RESPONSE_TEMPLATE), anyString(), templateParameters.capture(), anyString());

        assertThat(templateParameters.getValue())
            .containsEntry(NotificationTemplateParameters.FRONTEND_BASE_URL, FRONTEND_BASE_URL);
    }

    @Test
    public void notifyClaimantWhenDefendantRespondsWithAdmissions() throws Exception {
        Claim claim = SampleClaim
            .getWithResponse(SampleResponse
                .PartAdmission.builder()
                .buildWithPaymentOptionBySpecifiedDate());
        String reference = claim.getReferenceNumber();

        when(emailTemplates.getDefendantAdmissionResponseToClaimant())
            .thenReturn(DEFENDANT_RESPOND_BY_ADMISSION);

        service.notifyClaimant(claim, reference);

        verify(notificationClient)
            .sendEmail(eq(DEFENDANT_RESPOND_BY_ADMISSION), eq(claim.getSubmitterEmail()), anyMap(), eq(reference));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenResponseNotPresent() {
        Claim claimWithNoResponse = SampleClaim.builder().build();

        String reference = claimWithNoResponse.getReferenceNumber();

        service.notifyClaimant(claimWithNoResponse, reference);

        verifyZeroInteractions(emailTemplates, notificationClient);
    }
}
