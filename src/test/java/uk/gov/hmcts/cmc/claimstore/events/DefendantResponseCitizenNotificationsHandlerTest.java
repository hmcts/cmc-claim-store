package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.notifications.DefendantResponseNotificationService;
import uk.gov.service.notify.NotificationClientException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseCitizenNotificationsHandlerTest {
    private DefendantResponseCitizenNotificationsHandler defendantResponseCitizenNotificationsHandler;
    private static final DefendantResponseEvent RESPONSE_EVENT = new DefendantResponseEvent(
        SampleClaimIssuedEvent.CLAIM, SampleClaimIssuedEvent.DEFENDANT_RESPONSE
    );

    @Mock
    private DefendantResponseNotificationService defendantResponseNotificationService;

    @Before
    public void setup() {
        defendantResponseCitizenNotificationsHandler
            = new DefendantResponseCitizenNotificationsHandler(defendantResponseNotificationService);
    }

    @Test
    public void notifyDefendantResponseSendsNotificationsToDefendant() throws NotificationClientException {

        defendantResponseCitizenNotificationsHandler.notifyDefendantResponse(RESPONSE_EVENT);

        verify(defendantResponseNotificationService, once()).notifyDefendant(
            eq(SampleClaimIssuedEvent.CLAIM),
            eq(SampleClaimIssuedEvent.DEFENDANT_EMAIL),
            eq("defendant-response-notification-" + RESPONSE_EVENT.getClaim().getReferenceNumber())
        );
    }

    @Test
    public void notifyDefendantResponseSendsNotificationsToClaimant() throws NotificationClientException {

        defendantResponseCitizenNotificationsHandler.notifyClaimantResponse(RESPONSE_EVENT);

        verify(defendantResponseNotificationService, once()).notifyClaimant(
            eq(SampleClaimIssuedEvent.CLAIM),
            eq(SampleClaimIssuedEvent.DEFENDANT_RESPONSE),
            eq(SampleClaimIssuedEvent.CLAIM.getSubmitterEmail()),
            eq("claimant-response-notification-" + RESPONSE_EVENT.getClaim().getReferenceNumber())
        );
    }
}
