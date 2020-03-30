package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.settlement.SignSettlementAgreementActionsHandler;
import uk.gov.hmcts.cmc.claimstore.events.settlement.SignSettlementAgreementEvent;
import uk.gov.hmcts.cmc.claimstore.services.notifications.SettlementAgreementNotificationService;
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
public class SignSettlementAgreementActionsHandlerTest {

    private static final String FRONTEND_URL = "domain";
    private static final String SETTLEMENT_SIGNED_TO_DEFENDANT = "settlement agreement signed, email to defendant";
    private static final String SETTLEMENT_SIGNED_TO_CLAIMANT = "settlement agreement signed, email to claimant";
    private static final Claim claim = SampleClaim.builder().withSettlement(mock(Settlement.class)).build();

    private SignSettlementAgreementActionsHandler handler;

    @Mock
    private NotificationClient notificationClient;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;
    @Mock
    private AppInsights appInsights;

    @Before
    public void setUp() {
        when(notificationsProperties.getTemplates()).thenReturn(templates);
        when(notificationsProperties.getFrontendBaseUrl()).thenReturn(FRONTEND_URL);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getClaimantSignedSettlementAgreementToClaimant())
            .thenReturn(SETTLEMENT_SIGNED_TO_CLAIMANT);
        when(emailTemplates.getClaimantSignedSettlementAgreementToDefendant())
            .thenReturn(SETTLEMENT_SIGNED_TO_DEFENDANT);

        SettlementAgreementNotificationService notificationService =
            new SettlementAgreementNotificationService(notificationClient, notificationsProperties, appInsights);

        handler = new SignSettlementAgreementActionsHandler(notificationService);
    }

    @Test
    public void shouldSendNotificationsToClaimantWhenOfferAccepted() throws NotificationClientException {
        SignSettlementAgreementEvent event = new SignSettlementAgreementEvent(claim);

        handler.sendNotificationToClaimant(event);

        verify(notificationClient).sendEmail(
            eq(SETTLEMENT_SIGNED_TO_CLAIMANT),
            eq(event.getClaim().getSubmitterEmail()),
            anyMap(),
            eq("to-claimant-claimant’s-response-submitted-notification-000MC001")
        );
    }

    @Test
    public void shouldSendNotificationsToDefendantWhenOfferAccepted() throws NotificationClientException {
        SignSettlementAgreementEvent event = new SignSettlementAgreementEvent(claim);

        handler.sendNotificationToDefendant(event);

        verify(notificationClient).sendEmail(
            eq(SETTLEMENT_SIGNED_TO_DEFENDANT),
            eq(event.getClaim().getDefendantEmail()),
            anyMap(),
            eq("to-defendant-claimant’s-response-submitted-notification-000MC001")
        );
    }
}
