package uk.gov.hmcts.cmc.claimstore.events.response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
public class MoreTimeRequestedCitizenNotificationHandlerTest {

    private static final String FRONTEND_URL = "domain";
    private static final String DEFENDANT_TEMPLATE_ID = "defendant template id";

    private MoreTimeRequestedCitizenNotificationHandler handler;

    @Mock
    private MoreTimeRequestedNotificationService moreTimeRequestedNotificationService;

    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;

    @Before
    public void setup() {
        when(notificationsProperties.getTemplates()).thenReturn(templates);
        when(notificationsProperties.getFrontendBaseUrl()).thenReturn(FRONTEND_URL);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getDefendantMoreTimeRequested()).thenReturn(DEFENDANT_TEMPLATE_ID);

        handler = new MoreTimeRequestedCitizenNotificationHandler(
            moreTimeRequestedNotificationService,
            notificationsProperties
        );
    }

    @Test
    public void sendNotificationsSendsNotificationsToDefendant() {

        MoreTimeRequestedEvent event = SampleMoreTimeRequestedEvent.getDefault();

        handler.sendNotifications(event);

        verify(moreTimeRequestedNotificationService, once()).sendMail(
            eq(event.getDefendantEmail()),
            eq(DEFENDANT_TEMPLATE_ID),
            anyMap(),
            eq(SampleMoreTimeRequestedEvent.getReference("defendant", event.getClaim().getReferenceNumber()))
        );
    }
}
