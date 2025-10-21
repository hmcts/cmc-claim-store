package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;

@ExtendWith(SpringExtension.class)
public class NotificationServiceTest extends BaseNotificationServiceTest {

    private static final String REFERENCE = "reference";
    private static final String TEMPLATE_ID = "templateId";
    private static final Map<String, String> PARAMETERS = new HashMap<>();

    private NotificationService service;

    @BeforeEach
    public void beforeEachTest() {
        service = new NotificationService(notificationClient, appInsights);
    }

    @Test
    public void shouldThrowNotificationExceptionWhenClientThrowsNotificationClientException() throws Exception {
        when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(mock(NotificationClientException.class));

        try {
            assertThrows(NotificationException.class, () -> {
                service.sendMail(USER_EMAIL, TEMPLATE_ID, PARAMETERS, REFERENCE);
            });
        } finally {
            verify(notificationClient).sendEmail(anyString(), anyString(), anyMap(), anyString());
        }
    }

    @Test
    public void shouldSendEmailUsingPredefinedTemplate() throws Exception {
        service.sendMail(USER_EMAIL, TEMPLATE_ID, PARAMETERS, REFERENCE);

        verify(notificationClient).sendEmail(eq(TEMPLATE_ID), eq(USER_EMAIL), eq(PARAMETERS), eq(REFERENCE));
    }

    @Test
    public void recoveryShouldNotLogPII() {
        NotificationException exception = new NotificationException("expected exception");
        ImmutableMap<String, String> reference = ImmutableMap.of(CLAIM_REFERENCE_NUMBER, "reference");
        try {
            service.logNotificationFailure(
                exception,
                null,
                "hidden@email.com",
                reference,
                "reference"
            );
            Assertions.fail("Expected a NotificationException to be thrown");
        } catch (NotificationException expected) {
            assertWasLogged("Failure: failed to send notification (reference) due to expected exception");
            assertWasNotLogged("hidden@email.com");
        }
    }
}
