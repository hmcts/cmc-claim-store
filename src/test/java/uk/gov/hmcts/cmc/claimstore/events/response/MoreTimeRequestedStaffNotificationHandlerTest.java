package uk.gov.hmcts.cmc.claimstore.events.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleMoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@ExtendWith(MockitoExtension.class)
public class MoreTimeRequestedStaffNotificationHandlerTest {

    private static final String STAFF_EMAIL_ADDRESS = "staff@example.com";
    private static final String STAFF_TEMPLATE_ID = "staff template id";

    private MoreTimeRequestedStaffNotificationHandler handler;

    @Mock
    private NotificationService notificationService;

    @Mock
    private StaffEmailProperties staffEmailProperties;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;

    @BeforeEach
    public void setup() {
        lenient().when(staffEmailProperties.getRecipient()).thenReturn(STAFF_EMAIL_ADDRESS);

        lenient().when(notificationsProperties.getTemplates()).thenReturn(templates);
        lenient().when(templates.getEmail()).thenReturn(emailTemplates);
        lenient().when(emailTemplates.getStaffMoreTimeRequested()).thenReturn(STAFF_TEMPLATE_ID);
    }

    @Test
    public void sendNotificationsSendsNotificationsToStaff() {
        handler = new MoreTimeRequestedStaffNotificationHandler(
                notificationService,
            notificationsProperties,
            staffEmailProperties,
            true
        );

        MoreTimeRequestedEvent event = SampleMoreTimeRequestedEvent.getDefault();

        handler.sendNotifications(event);

        verify(notificationService, once()).sendMail(
            eq(STAFF_EMAIL_ADDRESS),
            eq(STAFF_TEMPLATE_ID),
            anyMap(),
            eq(SampleMoreTimeRequestedEvent.getReference("staff", event.getClaim().getReferenceNumber()))
        );
    }

    @Test
    public void shouldNotSendNotificationsSendsNotificationsToStaffWhenStaffEmailsDisabled() {
        handler = new MoreTimeRequestedStaffNotificationHandler(
                notificationService,
            notificationsProperties,
            staffEmailProperties,
            false
        );

        MoreTimeRequestedEvent event = SampleMoreTimeRequestedEvent.getDefault();

        handler.sendNotifications(event);

        verify(notificationService, never()).sendMail(
            anyString(),
            anyString(),
            anyMap(),
            anyString()
        );
    }
}
