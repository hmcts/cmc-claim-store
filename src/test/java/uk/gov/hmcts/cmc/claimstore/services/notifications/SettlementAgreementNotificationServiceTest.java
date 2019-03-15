package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NOTIFICATION_FAILURE;

@RunWith(MockitoJUnitRunner.class)
public class SettlementAgreementNotificationServiceTest extends BaseNotificationServiceTest {

    public static final String REFERENCE = "reference";
    private SettlementAgreementNotificationService service;

    @Before
    public void setUp() {
        super.setUp();
        service = new SettlementAgreementNotificationService(notificationClient, properties, appInsights);
    }

    @Test(expected = NotificationException.class)
    public void emailClaimantShouldThrowRuntimeExceptionWhenNotificationClientThrows() throws Exception {
        when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(mock(NotificationClientException.class));

        service.sendNotificationEmail(USER_EMAIL,
            CLAIMANT_SIGNED_SETTLEMENT_AGREEMENT_TO_CLAIMANT_TEMPLATE,
            new HashMap<>(),
            REFERENCE);

        verify(appInsights).trackEvent(eq(NOTIFICATION_FAILURE), eq(REFERENCE_NUMBER), eq(REFERENCE));
    }
    
    @Test
    public void recoveryShouldNotLogPII() {
        service.logNotificationFailure(
            new NotificationException("expected exception"),
            null,
            "hidden@email.com",
            null,
            REFERENCE
        );

        assertWasLogged("Failure: failed to send notification (reference) due to expected exception");
        assertWasNotLogged("hidden@email.com");
    }
}
