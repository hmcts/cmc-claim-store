package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@RunWith(MockitoJUnitRunner.class)
public class AgreementCounterSignedCitizenActionsHandlerTest {
    private static final String FRONTEND_URL = "domain";
    private static final String ORIGINATOR_TEMPLATE_ID = "originator template id";
    private static final String OTHER_PARTY_TEMPLATE_ID = "other party template id";
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final AgreementCountersignedEvent EVENT_BY_CLAIMANT =
        new AgreementCountersignedEvent(SampleClaim.getDefault(), MadeBy.CLAIMANT, AUTHORISATION);
    private static final AgreementCountersignedEvent EVENT_BY_DEFENDANT =
        new AgreementCountersignedEvent(SampleClaim.getDefault(), MadeBy.DEFENDANT, AUTHORISATION);

    private AgreementCounterSignedCitizenActionsHandler handler;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private NotificationTemplates templates;

    @Mock
    private EmailTemplates emailTemplates;

    @Captor
    private ArgumentCaptor<Map<String, String>> paramsCaptor;

    @Before
    public void setup() {
        when(notificationsProperties.getTemplates()).thenReturn(templates);
        when(notificationsProperties.getFrontendBaseUrl()).thenReturn(FRONTEND_URL);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getOfferCounterSignedEmailToOriginator()).thenReturn(ORIGINATOR_TEMPLATE_ID);
        when(emailTemplates.getOfferCounterSignedEmailToOtherParty()).thenReturn(OTHER_PARTY_TEMPLATE_ID);

        handler = new AgreementCounterSignedCitizenActionsHandler(notificationService, notificationsProperties);
    }

    @Test
    public void sendNotificationToOtherPartyByClaimantShouldEmailDefendant() {
        handler.sendNotificationToOtherParty(EVENT_BY_CLAIMANT);

        verify(notificationService).sendMail(
            eq(EVENT_BY_CLAIMANT.getClaim().getDefendantEmail()),
            eq(OTHER_PARTY_TEMPLATE_ID),
            paramsCaptor.capture(),
            eq("to-defendant-agreement-counter-signed-by-claimant-notification-000MC001")
        );

        verifyParameters();
    }

    @Test
    public void sendNotificationToOtherPartyByDefendantShouldEmailClaimant() {
        handler.sendNotificationToOtherParty(EVENT_BY_DEFENDANT);

        verify(notificationService).sendMail(
            eq(EVENT_BY_CLAIMANT.getClaim().getSubmitterEmail()),
            eq(OTHER_PARTY_TEMPLATE_ID),
            paramsCaptor.capture(),
            eq("to-claimant-agreement-counter-signed-by-defendant-notification-000MC001")
        );

        verifyParameters();
    }

    @Test
    public void sendNotificationToOfferOriginatorByClaimantShouldEmailClaimant() {
        handler.sendNotificationToOfferOriginator(EVENT_BY_CLAIMANT);

        verify(notificationService).sendMail(
            eq(EVENT_BY_CLAIMANT.getClaim().getSubmitterEmail()),
            eq(ORIGINATOR_TEMPLATE_ID),
            paramsCaptor.capture(),
            eq("to-claimant-agreement-counter-signed-by-claimant-notification-000MC001")
        );

        verifyParameters();
    }

    @Test
    public void sendNotificationToOfferOriginatorByDefendantShouldEmailDefendant() {
        handler.sendNotificationToOfferOriginator(EVENT_BY_DEFENDANT);

        verify(notificationService).sendMail(
            eq(EVENT_BY_CLAIMANT.getClaim().getDefendantEmail()),
            eq(ORIGINATOR_TEMPLATE_ID),
            paramsCaptor.capture(),
            eq("to-defendant-agreement-counter-signed-by-defendant-notification-000MC001")
        );

        verifyParameters();
    }

    private void verifyParameters() {
        Map<String, String> params = paramsCaptor.getValue();
        assertThat(params).containsOnlyKeys(CLAIMANT_NAME, DEFENDANT_NAME, CLAIM_REFERENCE_NUMBER, FRONTEND_BASE_URL);
    }
}
