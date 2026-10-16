package uk.gov.hmcts.cmc.claimstore.events.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.notifications.DefendantResponseNotificationService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
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

    @Test
    public void throwExceptionWhenResponseNotPresent() {
        assertThrows(IllegalArgumentException.class, () -> {
            defendantResponseCitizenNotificationsHandler.notifyClaimantResponse(RESPONSE_EVENT_WITHOUT_RESPONSE);
        });
    }

    @Test
    public void throwExceptionResponseEventIsGeneratedWithNullClaim() {
        assertThrows(NullPointerException.class, () -> {
            DefendantResponseEvent responseEventWithNullClaim = new DefendantResponseEvent(null, AUTHORISATION);

            defendantResponseCitizenNotificationsHandler.notifyClaimantResponse(responseEventWithNullClaim);
        });
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

    @Test
    public void throwExceptionWhenResponseNotPresentDefendantPaperResponse() {
        assertThrows(IllegalArgumentException.class, () -> {
            DefendantPaperResponseEvent responseEventWithoutResponse = new DefendantPaperResponseEvent(
                SampleClaimIssuedEvent.CLAIM_NO_RESPONSE,
                AUTHORISATION
            );

            defendantResponseCitizenNotificationsHandler.notifyClaimantResponse(responseEventWithoutResponse);
        });
    }

    @Test
    public void throwExceptionResponseEventIsGeneratedWithNullClaimDefendantPaperResponse() {
        assertThrows(NullPointerException.class, () -> {
            DefendantPaperResponseEvent responseEventWithNullClaim = new DefendantPaperResponseEvent(null, AUTHORISATION);

            defendantResponseCitizenNotificationsHandler.notifyClaimantResponse(responseEventWithNullClaim);
        });
    }
}
