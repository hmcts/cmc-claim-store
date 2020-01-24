package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OfferRespondedCitizenActionsHandlerTest {

    private static final String FRONTEND_URL = "domain";
    private static final String OFFER_ACCEPTED_TO_DEFENDANT = "offer accepted, email to defendant";
    private static final String OFFER_ACCEPTED_TO_CLAIMANT = "offer accepted, email to claimant";
    private static final String OFFER_REJECTED_TO_DEFENDANT = "offer rejected, email to defendant";
    private static final String OFFER_REJECTED_TO_CLAIMANT = "offer rejected, email to claimant";
    private static final Claim claimWithOffer = SampleClaim.builder().withSettlement(mock(Settlement.class)).build();

    private OfferRespondedCitizenActionsHandler handler;

    @Mock
    private NotificationService notificationService;

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
        when(emailTemplates.getOfferAcceptedByClaimantEmailToDefendant()).thenReturn(OFFER_ACCEPTED_TO_DEFENDANT);
        when(emailTemplates.getOfferAcceptedByClaimantEmailToClaimant()).thenReturn(OFFER_ACCEPTED_TO_CLAIMANT);
        when(emailTemplates.getOfferRejectedByClaimantEmailToDefendant()).thenReturn(OFFER_REJECTED_TO_DEFENDANT);
        when(emailTemplates.getOfferRejectedByClaimantEmailToClaimant()).thenReturn(OFFER_REJECTED_TO_CLAIMANT);

        handler = new OfferRespondedCitizenActionsHandler(notificationService, notificationsProperties);
    }

    @Test
    public void shouldSendNotificationsToClaimantWhenOfferAccepted() {

        OfferAcceptedEvent event = new OfferAcceptedEvent(claimWithOffer, MadeBy.CLAIMANT);

        handler.sendNotificationToClaimantOnOfferAcceptedByClaimant(event);

        verify(notificationService).sendMail(
            eq(event.getClaim().getSubmitterEmail()),
            eq(OFFER_ACCEPTED_TO_CLAIMANT),
            anyMap(),
            eq("to-claimant-offer-accepted-by-claimant-notification-000CM001")
        );
    }

    @Test
    public void shouldSendNotificationsToDefendantWhenOfferAccepted() {

        OfferAcceptedEvent event = new OfferAcceptedEvent(claimWithOffer, MadeBy.CLAIMANT);

        handler.sendNotificationToDefendantOnOfferAcceptedByClaimant(event);

        verify(notificationService).sendMail(
            eq(event.getClaim().getDefendantEmail()),
            eq(OFFER_ACCEPTED_TO_DEFENDANT),
            anyMap(),
            eq("to-defendant-offer-accepted-by-claimant-notification-000CM001")
        );
    }

    @Test
    public void shouldSendNotificationsToClaimantWhenOfferRejected() {

        OfferRejectedEvent event = new OfferRejectedEvent(claimWithOffer, MadeBy.CLAIMANT);

        handler.sendNotificationToClaimantOnOfferRejectedByClaimant(event);

        verify(notificationService).sendMail(
            eq(event.getClaim().getSubmitterEmail()),
            eq(OFFER_REJECTED_TO_CLAIMANT),
            anyMap(),
            eq("to-claimant-offer-rejected-by-claimant-notification-000CM001")
        );
    }

    @Test
    public void shouldSendNotificationsToDefendantWhenOfferRejected() {

        OfferRejectedEvent event = new OfferRejectedEvent(claimWithOffer, MadeBy.CLAIMANT);

        handler.sendNotificationToDefendantOnOfferRejectedByClaimant(event);

        verify(notificationService).sendMail(
            eq(event.getClaim().getDefendantEmail()),
            eq(OFFER_REJECTED_TO_DEFENDANT),
            anyMap(),
            eq("to-defendant-offer-rejected-by-claimant-notification-000CM001")
        );
    }
}
