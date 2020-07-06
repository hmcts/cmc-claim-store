package uk.gov.hmcts.cmc.claimstore.events.response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.notifications.DefendantResponseNotificationService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseCitizenNotificationsHandlerTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final DefendantResponseEvent RESPONSE_EVENT = new DefendantResponseEvent(
        SampleClaimIssuedEvent.CLAIM_WITH_RESPONSE,
        AUTHORISATION
    );

    private static final DefendantResponseEvent RESPONSE_EVENT_WITHOUT_RESPONSE = new DefendantResponseEvent(
        SampleClaimIssuedEvent.CLAIM_NO_RESPONSE,
        AUTHORISATION
    );

    private DefendantResponseCitizenNotificationsHandler defendantResponseCitizenNotificationsHandler;

    @Mock
    private DefendantResponseNotificationService defendantResponseNotificationService;

    @Before
    public void setup() {
        defendantResponseCitizenNotificationsHandler
            = new DefendantResponseCitizenNotificationsHandler(defendantResponseNotificationService);
    }

    @Test
    public void notifyDefendantResponseSendsNotificationsToDefendant() {

        defendantResponseCitizenNotificationsHandler.notifyDefendantResponse(RESPONSE_EVENT);

        verify(defendantResponseNotificationService, once()).notifyDefendant(
            eq(SampleClaimIssuedEvent.CLAIM_WITH_RESPONSE),
            eq(SampleClaimIssuedEvent.DEFENDANT_EMAIL),
            eq("defendant-response-notification-" + RESPONSE_EVENT.getClaim().getReferenceNumber())
        );
    }

    @Test
    public void notifyDefendantResponseSendsNotificationsToClaimant() {

        defendantResponseCitizenNotificationsHandler.notifyClaimantResponse(RESPONSE_EVENT);

        verify(defendantResponseNotificationService, once()).notifyClaimant(
            eq(SampleClaimIssuedEvent.CLAIM_WITH_RESPONSE),
            eq("claimant-response-notification-" + RESPONSE_EVENT.getClaim().getReferenceNumber())
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenResponseNotPresent() {

        defendantResponseCitizenNotificationsHandler.notifyClaimantResponse(RESPONSE_EVENT_WITHOUT_RESPONSE);

    }

    @Test(expected = NullPointerException.class)
    public void throwExceptionResponseEventIsGeneratedWithNullClaim() {

        DefendantResponseEvent responseEventWithNullClaim = new DefendantResponseEvent(null, AUTHORISATION);

        defendantResponseCitizenNotificationsHandler.notifyClaimantResponse(responseEventWithNullClaim);
    }

    public void notifyDefendantPaperResponseSendsNotificationsToClaimant() {
        DefendantPaperResponseEvent responseEvent = new DefendantPaperResponseEvent(
            SampleClaimIssuedEvent.CLAIM_WITH_RESPONSE,
            AUTHORISATION
        );

        defendantResponseCitizenNotificationsHandler.notifyClaimantResponse(responseEvent);

        verify(defendantResponseNotificationService, once()).notifyClaimant(
            eq(SampleClaimIssuedEvent.CLAIM_WITH_RESPONSE),
            eq("claimant-response-notification-" + RESPONSE_EVENT.getClaim().getReferenceNumber())
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenResponseNotPresentDefendantPaperResponse() {

        DefendantPaperResponseEvent responseEventWithoutResponse = new DefendantPaperResponseEvent(
            SampleClaimIssuedEvent.CLAIM_NO_RESPONSE,
            AUTHORISATION
        );

        defendantResponseCitizenNotificationsHandler.notifyClaimantResponse(responseEventWithoutResponse);

    }

    @Test(expected = NullPointerException.class)
    public void throwExceptionResponseEventIsGeneratedWithNullClaimDefendantPaperResponse() {

        DefendantPaperResponseEvent responseEventWithNullClaim = new DefendantPaperResponseEvent(null, AUTHORISATION);

        defendantResponseCitizenNotificationsHandler.notifyClaimantResponse(responseEventWithNullClaim);
    }
}
