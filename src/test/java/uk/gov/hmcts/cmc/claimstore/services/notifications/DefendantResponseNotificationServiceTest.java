package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.FreeMediationDecisionDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.service.notify.NotificationClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseNotificationServiceTest extends BaseNotificationServiceTest {

    private final String reference = "defendant-response-notification-" + claim.getReferenceNumber();
    private DefendantResponseNotificationService service;

    @Before
    public void beforeEachTest() {
        service = new DefendantResponseNotificationService(
            notificationClient, new FreeMediationDecisionDateCalculator(28), properties
        );

        when(properties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(properties.getTemplates()).thenReturn(templates);
        when(emailTemplates.getDefendantResponseIssuedToIndividual()).thenReturn(DEFENDANT_RESPONSE_TEMPLATE);
    }

    @Test(expected = NotificationException.class)
    public void notifyDefendantShouldThrowRuntimeExceptionWhenNotificationClientThrows() throws Exception {
        when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(mock(NotificationClientException.class));

        service.notifyDefendant(claim, USER_EMAIL, reference);
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
    public void notifyDefendantShouldUseDefendantResponseIssuedToIndividualEmailTemplate() throws Exception {
        service.notifyDefendant(SampleClaim.getClaimWithFullDefenceNoMediation(), USER_EMAIL, reference);

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

    @Test (expected = IllegalArgumentException.class)
    public void throwExceptionWhenResponseNotPresent() {
        Claim claimWithNoResponse = SampleClaim.builder().build();

        String reference = claimWithNoResponse.getReferenceNumber();

        service.notifyClaimant(claimWithNoResponse, reference);

        verifyZeroInteractions(emailTemplates, notificationClient);
    }

}
