package uk.gov.hmcts.cmc.claimstore.events.response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.emails.StaffEmailProperties;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleMoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.services.notifications.MoreTimeRequestedNotificationService;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class MoreTimeRequestedStaffNotificationHandlerTest {

    private static final String STAFF_EMAIL_ADDRESS = "staff@example.com";
    private static final String STAFF_TEMPLATE_ID = "staff template id";

    private MoreTimeRequestedStaffNotificationHandler handler;

    @Mock
    private MoreTimeRequestedNotificationService moreTimeRequestedNotificationService;

    @Mock
    private StaffEmailProperties staffEmailProperties;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;

    @Before
    public void setup() {
        when(staffEmailProperties.getRecipient()).thenReturn(STAFF_EMAIL_ADDRESS);

        when(notificationsProperties.getTemplates()).thenReturn(templates);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getStaffMoreTimeRequested()).thenReturn(STAFF_TEMPLATE_ID);

        handler = new MoreTimeRequestedStaffNotificationHandler(
            moreTimeRequestedNotificationService,
            notificationsProperties,
            staffEmailProperties
        );
    }

    @Test
    public void sendNotificationsSendsNotificationsToStaff() {

        MoreTimeRequestedEvent event = SampleMoreTimeRequestedEvent.getDefault();

        handler.sendNotifications(event);

        verify(moreTimeRequestedNotificationService, once()).sendMail(
            eq(STAFF_EMAIL_ADDRESS),
            eq(STAFF_TEMPLATE_ID),
            anyMap(),
            eq(SampleMoreTimeRequestedEvent.getReference("staff", event.getClaim().getReferenceNumber()))
        );
    }
}
