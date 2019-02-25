package uk.gov.hmcts.cmc.claimstore.events.response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.cmc.claimstore.documents.DefendantResponseReceiptService;
import uk.gov.hmcts.cmc.claimstore.events.DocumentUploadEvent;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.notifications.DefendantResponseNotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class DefendantResponseCitizenNotificationsHandlerTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
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
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private DefendantResponseReceiptService defendantResponseReceiptService;

    @Before
    public void setup() {
        defendantResponseCitizenNotificationsHandler
            = new DefendantResponseCitizenNotificationsHandler(defendantResponseNotificationService,
            publisher,
            defendantResponseReceiptService);
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
    public void notifyDefendantResponseTriggersDocumentGeneratedEvent() {
        defendantResponseCitizenNotificationsHandler.uploadDocument(RESPONSE_EVENT);
        verify(publisher).publishEvent(any(DocumentUploadEvent.class));
    }

    @Test
    public void uploadDocumentThrowsExceptionWhenResponseNotPresent() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Response must be present");
        defendantResponseCitizenNotificationsHandler.uploadDocument(RESPONSE_EVENT_WITHOUT_RESPONSE);
    }

    @Test
    public void uploadDocumentThrowsExceptionWhenClaimNotPresent() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("Claim must be present");
        defendantResponseCitizenNotificationsHandler.uploadDocument(new DefendantResponseEvent(null, AUTHORISATION));
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
}
