package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest extends BaseNotificationServiceTest {

    private static final String REFERENCE = "reference";
    private static final String TEMPLATE_ID = "templateId";
    private static final Map<String, String> PARAMETERS = new HashMap<>();

    private NotificationService service;

    @Before
    public void beforeEachTest() {
        service = new NotificationService(notificationClient, appInsights, false);
    }

    @Test(expected = NotificationException.class)
    public void shouldThrowNotificationExceptionWhenClientThrowsNotificationClientException() throws Exception {
        when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(mock(NotificationClientException.class));

        service.sendMail(USER_EMAIL, TEMPLATE_ID, PARAMETERS, REFERENCE);
        verify(appInsights).trackEvent(AppInsightsEvent.NOTIFICATION_FAILURE, REFERENCE_NUMBER, REFERENCE);
    }

    @Test
    public void shouldSendEmailUsingPredefinedTemplate() throws Exception {
        service.sendMail(USER_EMAIL, TEMPLATE_ID, PARAMETERS, REFERENCE);

        verify(notificationClient).sendEmail(eq(TEMPLATE_ID), eq(USER_EMAIL), eq(PARAMETERS), eq(REFERENCE));
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
