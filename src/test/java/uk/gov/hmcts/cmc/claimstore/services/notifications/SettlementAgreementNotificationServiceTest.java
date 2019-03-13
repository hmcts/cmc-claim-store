package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;

@RunWith(MockitoJUnitRunner.class)
public class SettlementAgreementNotificationServiceTest extends BaseNotificationServiceTest {

    private SettlementAgreementNotificationService service;

    @Before
    public void setUp() {
        super.setUp();
        service = new SettlementAgreementNotificationService(notificationClient, properties, appInsights);
    }

    @Test
    public void recoveryShouldNotLogPII() {
        service.logNotificationFailure(
            new NotificationException("expected exception"),
            null,
            "hidden@email.com",
            null,
            "reference"
        );

        assertWasLogged("Failure: failed to send notification (reference) due to expected exception");
        assertWasNotLogged("hidden@email.com");
    }
}
