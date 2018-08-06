package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.FreeMediationDecisionDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.service.notify.NotificationClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
        when(emailTemplates.getDefendantResponseWithNoMediationIssued())
            .thenReturn(DEFENDANT_RESPONSE_NO_MEDIATION_TEMPLATE);
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
            .sendEmail(eq(DEFENDANT_RESPONSE_NO_MEDIATION_TEMPLATE), eq(USER_EMAIL), anyMap(), eq(reference));
    }

    @Test
    public void notifyDefendantShouldPassFrontendHostInTemplateParameters() throws Exception {
        service.notifyDefendant(claim, USER_EMAIL, reference);

        verify(notificationClient).sendEmail(
            eq(DEFENDANT_RESPONSE_TEMPLATE), anyString(), templateParameters.capture(), anyString());

        assertThat(templateParameters.getValue())
            .containsEntry(NotificationTemplateParameters.FRONTEND_BASE_URL, FRONTEND_BASE_URL);
    }
}
