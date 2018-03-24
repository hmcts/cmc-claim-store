package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
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
public class ClaimIssuedNotificationServiceTest extends BaseNotificationServiceTest {

    private final String reference = "claimant-issue-notification-" + claim.getReferenceNumber();
    private ClaimIssuedNotificationService service;

    @Before
    public void beforeEachTest() {
        service = new ClaimIssuedNotificationService(notificationClient, properties);
        when(properties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        when(properties.getRespondToClaimUrl()).thenReturn(RESPOND_TO_CLAIM_URL);
    }

    @Test(expected = NotificationException.class)
    public void emailClaimantShouldThrowRuntimeExceptionWhenNotificationClientThrows() throws Exception {
        when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(mock(NotificationClientException.class));

        service.sendMail(claim, USER_EMAIL, null, CLAIMANT_CLAIM_ISSUED_TEMPLATE, reference, USER_FULLNAME);
    }

    @Test
    public void emailClaimantShouldSendEmailUsingPredefinedTemplate() throws Exception {
        service.sendMail(claim, USER_EMAIL, null, CLAIMANT_CLAIM_ISSUED_TEMPLATE, reference, USER_FULLNAME);

        verify(notificationClient).sendEmail(
            eq(CLAIMANT_CLAIM_ISSUED_TEMPLATE), anyString(), anyMap(), anyString());
    }

    @Test
    public void emailClaimantShouldSendToClaimantEmail() throws Exception {
        service.sendMail(claim, USER_EMAIL, null, CLAIMANT_CLAIM_ISSUED_TEMPLATE, reference, USER_FULLNAME);

        verify(notificationClient).sendEmail(
            anyString(), eq(USER_EMAIL), anyMap(), anyString());
    }

    @Test
    public void emailClaimantShouldPassClaimReferenceNumberInTemplateParameters() throws Exception {
        service.sendMail(claim, USER_EMAIL, null, CLAIMANT_CLAIM_ISSUED_TEMPLATE, reference, USER_FULLNAME);

        verify(notificationClient).sendEmail(
            eq(CLAIMANT_CLAIM_ISSUED_TEMPLATE), anyString(), templateParameters.capture(), anyString());

        assertThat(templateParameters.getValue())
            .containsEntry(NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
    }

    @Test
    public void emailClaimantShouldPassFrontendHostInTemplateParameters() throws Exception {
        service.sendMail(claim, USER_EMAIL, null, CLAIMANT_CLAIM_ISSUED_TEMPLATE, reference, USER_FULLNAME);

        verify(notificationClient).sendEmail(
            eq(CLAIMANT_CLAIM_ISSUED_TEMPLATE), anyString(), templateParameters.capture(), anyString());

        assertThat(templateParameters.getValue())
            .containsEntry(NotificationTemplateParameters.FRONTEND_BASE_URL, FRONTEND_BASE_URL);
    }

    @Test
    public void emailClaimantShouldPassNameInTemplateParameters() throws Exception {
        service.sendMail(claim, USER_EMAIL, null, CLAIMANT_CLAIM_ISSUED_TEMPLATE, reference, USER_FULLNAME);

        verify(notificationClient).sendEmail(
            eq(CLAIMANT_CLAIM_ISSUED_TEMPLATE), anyString(), templateParameters.capture(), anyString());

        String name = claim.getClaimData().getClaimant().getName();

        assertThat(templateParameters.getValue())
            .containsEntry(NotificationTemplateParameters.CLAIMANT_NAME, name);
    }

    @Test
    public void emailClaimantShouldPassRepresentativeNameInTemplateParameters() throws Exception {
        service.sendMail(SampleClaim.getDefaultForLegal(), USER_EMAIL, null,
            CLAIMANT_CLAIM_ISSUED_TEMPLATE, reference, USER_FULLNAME);

        verify(notificationClient).sendEmail(
            eq(CLAIMANT_CLAIM_ISSUED_TEMPLATE), anyString(), templateParameters.capture(), anyString());

        assertThat(templateParameters.getValue())
            .containsEntry(NotificationTemplateParameters.CLAIMANT_NAME, USER_FULLNAME);
    }

    @Test
    public void emailClaimantShouldUseClaimReferenceNumberForNotificationReference() throws Exception {
        service.sendMail(claim, USER_EMAIL, null, CLAIMANT_CLAIM_ISSUED_TEMPLATE, reference, USER_FULLNAME);

        verify(notificationClient).sendEmail(anyString(), anyString(),
            anyMap(), eq(reference));
    }

}
