package uk.gov.hmcts.cmc.claimstore.events.settlement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.SettlementRejected.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.SettlementRejected.referenceForDefendant;

@RunWith(MockitoJUnitRunner.class)
public class RejectSettlementAgreementActionsHandlerTest {

    private static final String CLAIMANT_TEMPLATE = "claimant-template";
    private static final String DEFENDANT_TEMPLATE = "defendant-template";

    @Mock
    private NotificationService service;

    @Mock
    private NotificationsProperties properties;

    @Mock
    private NotificationTemplates templates;

    @Mock
    private EmailTemplates emailTemplates;

    private RejectSettlementAgreementActionsHandler handler;

    @Before
    public void setUp() {
        handler = new RejectSettlementAgreementActionsHandler(service, properties);
        when(properties.getTemplates()).thenReturn(templates);
        when(properties.getFrontendBaseUrl()).thenReturn("http://localhost/");
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getSettlementRejectedEmailToClaimant()).thenReturn(CLAIMANT_TEMPLATE);
        when(emailTemplates.getSettlementRejectedEmailToDefendant()).thenReturn(DEFENDANT_TEMPLATE);
    }

    @Test
    public void shouldSendNotificationToDefendant() {
        handler.sendNotificationToDefendant(event());
        Mockito.verify(service).sendMail(
            eq(SampleTheirDetails.DEFENDANT_EMAIL),
            eq(DEFENDANT_TEMPLATE),
            anyMap(),
            eq(referenceForDefendant(SampleClaim.REFERENCE_NUMBER))
        );
    }

    @Test
    public void shouldSendNotificationToClaimant() {
        handler.sendNotificationToClaimant(event());
        Mockito.verify(service).sendMail(
            eq(SampleClaim.SUBMITTER_EMAIL),
            eq(CLAIMANT_TEMPLATE),
            anyMap(),
            eq(referenceForClaimant(SampleClaim.REFERENCE_NUMBER))
        );
    }

    private RejectSettlementAgreementEvent event() {
        return new RejectSettlementAgreementEvent(buildClaimWithSettlementAgreementRejected());
    }

    private Claim buildClaimWithSettlementAgreementRejected() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builder().build(), MadeBy.CLAIMANT);
        settlement.acceptCourtDetermination(MadeBy.CLAIMANT);
        settlement.reject(MadeBy.DEFENDANT);

        return SampleClaim.builder()
            .withDefendantEmail(SampleTheirDetails.DEFENDANT_EMAIL)
            .withSettlement(settlement)
            .build();
    }
}
