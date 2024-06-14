package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SettlementAgreementNotificationServiceTest extends BaseNotificationServiceTest {

    public static final String REFERENCE = "reference";
    private SettlementAgreementNotificationService service;

    @BeforeEach
    public void setUp() {
        super.setUp();
        service = new SettlementAgreementNotificationService(notificationClient, properties, appInsights);
    }

    @Test
    public void emailClaimantShouldThrowRuntimeExceptionWhenNotificationClientThrows() throws Exception {
        when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(mock(NotificationClientException.class));

        assertThrows(NotificationException.class, () -> {
            service.sendNotificationEmail(USER_EMAIL,
                CLAIMANT_SIGNED_SETTLEMENT_AGREEMENT_TO_CLAIMANT_TEMPLATE,
                new HashMap<>(),
                REFERENCE);
        });

        // verify(appInsights).trackEvent(eq(NOTIFICATION_FAILURE), eq(REFERENCE_NUMBER), eq(REFERENCE));
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
