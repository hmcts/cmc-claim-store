package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.OfferResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OfferMadeCitizenActionsHandlerTest {

    private static final String FRONTEND_URL = "domain";
    private static final String DEFENDANT_TEMPLATE_ID = "defendant template id";
    private static final String CLAIMANT_TEMPLATE_ID = "claimant template id";
    private static final OfferMadeEvent event = new OfferMadeEvent(SampleClaim.getDefault());

    private OfferMadeCitizenActionsHandler handler;

    @Mock
    private NotificationService notificationService;

    @Mock
    private OfferResponseDeadlineCalculator offerResponseDeadlineCalculator;

    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;

    @Before
    public void setup() {
        when(offerResponseDeadlineCalculator.calculateOfferResponseDeadline(any())).thenReturn(LocalDate.now());

        when(notificationsProperties.getTemplates()).thenReturn(templates);
        when(notificationsProperties.getFrontendBaseUrl()).thenReturn(FRONTEND_URL);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getDefendantOfferMade()).thenReturn(DEFENDANT_TEMPLATE_ID);
        when(emailTemplates.getClaimantOfferMade()).thenReturn(CLAIMANT_TEMPLATE_ID);

        handler = new OfferMadeCitizenActionsHandler(
            notificationService,
            offerResponseDeadlineCalculator,
            notificationsProperties
        );
    }

    @Test
    public void shouldSendNotificationsToClaimant() {

        handler.sendClaimantNotification(event);

        verify(notificationService).sendMail(
            eq(event.getClaim().getSubmitterEmail()),
            eq(CLAIMANT_TEMPLATE_ID),
            anyMap(),
            eq("claimant-offer-made-notification-000CM001")
        );
    }

    @Test
    public void shouldSendNotificationsToDefendant() {

        handler.sendDefendantNotification(event);

        verify(notificationService).sendMail(
            eq(event.getClaim().getDefendantEmail()),
            eq(DEFENDANT_TEMPLATE_ID),
            anyMap(),
            eq("defendant-offer-made-notification-000CM001")
        );
    }
}
