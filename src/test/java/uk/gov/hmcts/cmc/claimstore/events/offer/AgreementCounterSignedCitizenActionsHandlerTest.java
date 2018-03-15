package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.OfferMadeNotificationService;
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
public class AgreementCounterSignedCitizenActionsHandlerTest {
    private static final String FRONTEND_URL = "domain";
    private static final String OFFER_COUNTERSIGNED_BY_ORIGINATOR = "offer counter signed, email to originator";
    private static final String OFFER_COUNTERSIGNED_BY_OTHER_PARTY = "offer counter signed, email to other party";

    private static final Claim claimWithOffer
        = SampleClaim.builder().withSettlement(mock(Settlement.class)).build();

    private AgreementCounterSignedCitizenActionsHandler handler;

    @Mock
    private OfferMadeNotificationService offerNotificationService;

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
        when(emailTemplates.getOfferCounterSignedByOriginator()).thenReturn(OFFER_COUNTERSIGNED_BY_ORIGINATOR);
        when(emailTemplates.getOfferCounterSignedByOtherParty()).thenReturn(OFFER_COUNTERSIGNED_BY_OTHER_PARTY);

        handler = new AgreementCounterSignedCitizenActionsHandler(offerNotificationService, notificationsProperties);
    }

    @Test
    public void sendCounterSignedByClaimantNotificationToOtherParty() {
        AgreementCountersignedEvent event
            = new AgreementCountersignedEvent(claimWithOffer, MadeBy.CLAIMANT);

        handler.sendNotificationToOtherParty(event);

        verify(offerNotificationService).sendNotificationEmail(
            eq(event.getClaim().getDefendantEmail()),
            eq(OFFER_COUNTERSIGNED_BY_OTHER_PARTY),
            anyMap(),
            eq("to-defendant-agreement-counter-signed-by-claimant-notification-000CM001")
        );
    }

    @Test
    public void sendCounterSignedByDefendantNotificationToOtherParty() {
        AgreementCountersignedEvent event
            = new AgreementCountersignedEvent(claimWithOffer, MadeBy.DEFENDANT);

        handler.sendNotificationToOtherParty(event);

        verify(offerNotificationService).sendNotificationEmail(
            eq(event.getClaim().getSubmitterEmail()),
            eq(OFFER_COUNTERSIGNED_BY_OTHER_PARTY),
            anyMap(),
            eq("to-claimant-agreement-counter-signed-by-defendant-notification-000CM001")
        );
    }

    @Test
    public void sendCounterSignedByClaimantNotificationToOfferOriginator() {
        AgreementCountersignedEvent event
            = new AgreementCountersignedEvent(claimWithOffer, MadeBy.CLAIMANT);

        handler.sendNotificationToOfferOriginator(event);

        verify(offerNotificationService).sendNotificationEmail(
            eq(event.getClaim().getSubmitterEmail()),
            eq(OFFER_COUNTERSIGNED_BY_ORIGINATOR),
            anyMap(),
            eq("to-defendant-agreement-counter-signed-by-claimant-notification-000CM001")
        );
    }

    @Test
    public void sendCounterSignedByDefendantNotificationToOfferOriginator() {
        AgreementCountersignedEvent event
            = new AgreementCountersignedEvent(claimWithOffer, MadeBy.DEFENDANT);

        handler.sendNotificationToOfferOriginator(event);

        verify(offerNotificationService).sendNotificationEmail(
            eq(event.getClaim().getDefendantEmail()),
            eq(OFFER_COUNTERSIGNED_BY_ORIGINATOR),
            anyMap(),
            eq("to-claimant-agreement-counter-signed-by-defendant-notification-000CM001")
        );
    }
}
