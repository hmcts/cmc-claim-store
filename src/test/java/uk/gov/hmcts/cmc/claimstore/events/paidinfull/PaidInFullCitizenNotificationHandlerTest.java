package uk.gov.hmcts.cmc.claimstore.events.paidinfull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.notifications.BaseNotificationServiceTest;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.service.notify.NotificationClientException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.PaidInFull.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class PaidInFullCitizenNotificationHandlerTest extends BaseNotificationServiceTest {

    private PaidInFullCitizenNotificationHandler handler;

    @Mock
    private NotificationService notificationService;

    @Before
    public void setUp() {
        when(properties.getTemplates()).thenReturn(templates);
        when(properties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getClaimantSaysDefendantHasPaidInFull())
            .thenReturn(CLAIMANT_SAYS_DEFENDANT_PAID_IN_FULL_TEMPLATE);

        handler = new PaidInFullCitizenNotificationHandler(
            notificationService,
            properties
        );
    }

    @Test
    public void sendNotificationsSendsNotificationsToDefendantEmailNotLinked() throws NotificationClientException {
        PaidInFullEvent event = new PaidInFullEvent(SampleClaim.getDefault());

        handler.notifyDefendantForPaidInFull(event);
        verify(notificationService, once()).sendMail(
            eq(event.getClaim().getClaimData().getDefendant().getEmail().get()),
            eq(CLAIMANT_SAYS_DEFENDANT_PAID_IN_FULL_TEMPLATE),
            anyMap(),
            eq(referenceForDefendant(event.getClaim().getReferenceNumber()))
        );
    }

    @Test
    public void sendNotificationsSendsNotificationsToDefendantEmailLinked() throws NotificationClientException {
        Response fullDefenceResponse = SampleResponse.FullDefence.builder().build();
        PaidInFullEvent event =
            new PaidInFullEvent(SampleClaim.getWithResponseDefendantEmailVerified(fullDefenceResponse));

        handler.notifyDefendantForPaidInFull(event);
        verify(notificationService, once()).sendMail(
            eq(event.getClaim().getDefendantEmail()),
            eq(CLAIMANT_SAYS_DEFENDANT_PAID_IN_FULL_TEMPLATE),
            anyMap(),
            eq(referenceForDefendant(event.getClaim().getReferenceNumber()))
        );
    }

    @Test
    public void sendNotificationsDoesNotSendNotificationToDefendantWhenNoEmailAddress()
        throws NotificationClientException {
        PaidInFullEvent event = new PaidInFullEvent(SampleClaim.withClaimData());

            handler.notifyDefendantForPaidInFull(event);
        verify(notificationService, never()).sendMail(
            anyString(),
            anyString(),
            anyMap(),
            anyString()
        );
    }
}
