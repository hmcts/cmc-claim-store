package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.settlement.CountersignSettlementAgreementActionsHandler;
import uk.gov.hmcts.cmc.claimstore.events.settlement.CountersignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CountersignSettlementAgreementActionsHandlerTest {

    private static final String FRONTEND_URL = "domain";
    private static final String SETTLEMENT_SIGNED_TO_DEFENDANT = "settlement agreement signed, email to defendant";
    private static final String SETTLEMENT_SIGNED_TO_CLAIMANT = "settlement agreement signed, email to claimant";
    private static final Claim claim = SampleClaim.builder().withSettlement(mock(Settlement.class)).build();
    private static final String AUTHORISATION = "Bearer: aaa";

    private CountersignSettlementAgreementActionsHandler handler;

    @Mock
    private NotificationClient notificationClient;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;

    @Before
    public void setUp() {
        when(notificationsProperties.getTemplates()).thenReturn(templates);
        when(notificationsProperties.getFrontendBaseUrl()).thenReturn(FRONTEND_URL);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getDefendantSignedSettlementAgreementToClaimant())
            .thenReturn(SETTLEMENT_SIGNED_TO_CLAIMANT);
        when(emailTemplates.getDefendantSignedSettlementAgreementToDefendant())
            .thenReturn(SETTLEMENT_SIGNED_TO_DEFENDANT);

        NotificationService notificationService = new NotificationService(notificationClient);

        handler = new CountersignSettlementAgreementActionsHandler(notificationService, notificationsProperties);
    }

    @Test
    public void shouldSendNotificationsToClaimantWhenOfferAccepted() throws NotificationClientException {
        CountersignSettlementAgreementEvent event = new CountersignSettlementAgreementEvent(claim, AUTHORISATION);

        handler.sendNotificationToClaimant(event);

        verify(notificationClient).sendEmail(
            eq(SETTLEMENT_SIGNED_TO_CLAIMANT),
            eq(event.getClaim().getSubmitterEmail()),
            anyMap(),
            eq("to-claimant-agreement-counter-signed-by-defendant-notification-000CM001")
        );
    }

    @Test
    public void shouldSendNotificationsToDefendantWhenOfferAccepted() throws NotificationClientException {
        CountersignSettlementAgreementEvent event = new CountersignSettlementAgreementEvent(claim, AUTHORISATION);

        handler.sendNotificationToDefendant(event);

        verify(notificationClient).sendEmail(
            eq(SETTLEMENT_SIGNED_TO_DEFENDANT),
            eq(event.getClaim().getDefendantEmail()),
            anyMap(),
            eq("to-defendant-agreement-counter-signed-by-defendant-notification-000CM001")
        );
    }
}
