package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.service.notify.NotificationClientException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ClaimIssuedNotificationServiceTest extends BaseNotificationServiceTest {

    private final String reference = "claimant-issue-notification-" + claim.getReferenceNumber();
    private ClaimIssuedNotificationService service;

    @BeforeEach
    public void beforeEachTest() {
        service = new ClaimIssuedNotificationService(notificationClient, properties, appInsights);
        when(properties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        when(properties.getRespondToClaimUrl()).thenReturn(RESPOND_TO_CLAIM_URL);
    }

    @Test
    public void emailClaimantShouldThrowRuntimeExceptionWhenNotificationClientThrows() throws Exception {
        when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(mock(NotificationClientException.class));

        assertThrows(NotificationException.class, () -> {
            service.sendMail(claim, USER_EMAIL, null, CLAIMANT_CLAIM_ISSUED_TEMPLATE, reference, USER_FULLNAME);
        });

    }

    @Test
    public void emailClaimantShouldThrowExceptionIfIssuedOnDateIsMissing() {
        claim = claim.toBuilder().issuedOn(null).build();

        assertThrows(IllegalStateException.class, () -> {
            service.sendMail(claim, USER_EMAIL, null, CLAIMANT_CLAIM_ISSUED_TEMPLATE, reference, USER_FULLNAME);
        });
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

    @Test
    public void newFeatureFlagShouldBeTrueIfClaimHasFeatures() throws Exception {
        Claim claim = SampleClaim.builder().withFeatures(Collections.singletonList("sampleFeatures")).build();
        service.sendMail(claim, USER_EMAIL, null, CLAIMANT_CLAIM_ISSUED_TEMPLATE, reference, USER_FULLNAME);

        verify(notificationClient).sendEmail(anyString(), anyString(), templateParameters.capture(), anyString());

        assertThat(templateParameters.getValue()).containsEntry(NotificationTemplateParameters.NEW_FEATURES, "true");
    }

    @Test
    public void newFeatureFlagShouldBeFalseIfClaimHasNoFeatures() throws Exception {
        Claim claim = SampleClaim.builder().withFeatures(Collections.emptyList()).build();
        service.sendMail(claim, USER_EMAIL, null, CLAIMANT_CLAIM_ISSUED_TEMPLATE, reference, USER_FULLNAME);

        verify(notificationClient).sendEmail(anyString(), anyString(), templateParameters.capture(), anyString());

        assertThat(templateParameters.getValue()).containsEntry(NotificationTemplateParameters.NEW_FEATURES, "false");
    }

    @Test
    public void recoveryShouldNotLogPII() {
        NotificationException exception = new NotificationException("expected exception");
        try {
            service.logNotificationFailure(
                exception,
                null,
                "hidden@email.com",
                null,
                null,
                "reference",
                null
            );
            Assertions.fail("Expected a NotificationException to be thrown");
        } catch (NotificationException expected) {
            assertWasLogged("Failure: failed to send notification (reference) due to expected exception");
            assertWasNotLogged("hidden@email.com");
        }
    }
}
