package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimForHwF;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_INFO_REQUIRED_FOR_HWF;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NOTIFICATION_FAILURE;

@RunWith(MockitoJUnitRunner.class)
public class HwfClaimNotificationServiceTest extends BaseNotificationServiceTest {

    private final String reference = "hwf-claimant-notification-" + claim.getReferenceNumber();
    private HwfClaimNotificationService service;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void beforeEachTest() {
        claim = SampleClaimForHwF.getDefault().toBuilder().respondedAt(LocalDateTime.now())
            .lastEventTriggeredForHwfCase(MORE_INFO_REQUIRED_FOR_HWF.getValue()).build();
        service = new HwfClaimNotificationService(notificationClient, properties, appInsights);
        Mockito.when(properties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        Mockito.when(properties.getRespondToClaimUrl()).thenReturn(RESPOND_TO_CLAIM_URL);
    }

    @Test(expected = NotificationException.class)
    public void emailClaimantShouldThrowRuntimeExceptionWhenNotificationClientThrows() throws Exception {
        Mockito.when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(Mockito.mock(NotificationClientException.class));

        service.sendMail(claim, USER_EMAIL, HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE, reference, USER_FULLNAME);
        Mockito.verify(appInsights).trackEvent(NOTIFICATION_FAILURE, REFERENCE_NUMBER, reference);
    }

    @Test
    public void emailClaimantShouldSendEmailUsingPredefinedTemplate() throws Exception {
        service.sendMail(claim, USER_EMAIL, HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE, reference, USER_FULLNAME);

        Mockito.verify(notificationClient).sendEmail(
            eq(HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE), anyString(), anyMap(), anyString());
    }

    @Test
    public void emailClaimantShouldSendToClaimantEmail() throws Exception {
        service.sendMail(claim, USER_EMAIL, HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE, reference, USER_FULLNAME);

        Mockito.verify(notificationClient).sendEmail(
            anyString(), eq(USER_EMAIL), anyMap(), anyString());
    }

    @Test
    public void emailClaimantShouldPassClaimReferenceNumberInTemplateParameters() throws Exception {
        service.sendMail(claim, USER_EMAIL, HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE, reference, USER_FULLNAME);

        Mockito.verify(notificationClient).sendEmail(
            eq(HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE), anyString(), templateParameters.capture(), anyString());

        assertThat(templateParameters.getValue())
            .containsEntry(NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
    }

    @Test
    public void emailClaimantShouldPassNameInTemplateParameters() throws Exception {
        service.sendMail(claim, USER_EMAIL, HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE, reference, USER_FULLNAME);

        Mockito.verify(notificationClient).sendEmail(
            eq(HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE), anyString(), templateParameters.capture(), anyString());

        String name = claim.getClaimData().getClaimant().getName();

        assertThat(templateParameters.getValue())
            .containsEntry(NotificationTemplateParameters.CLAIMANT_NAME, name);
    }

    @Test
    public void emailClaimantShouldUseClaimReferenceNumberForNotificationReference() throws Exception {
        service.sendMail(claim, USER_EMAIL, HWF_CLAIMANT_CLAIM_CREATED_TEMPLATE, reference, USER_FULLNAME);

        Mockito.verify(notificationClient).sendEmail(anyString(), anyString(),
            anyMap(), eq(reference));
    }

    @Test
    public void recoveryShouldNotLogPII() {
        expectedException.expect(NotificationException.class);
        service.logNotificationFailure(
            new NotificationException("expected exception"),
            "reference"
        );

        assertWasLogged("Failure: failed to send notification (reference) due to expected exception");
    }
}
