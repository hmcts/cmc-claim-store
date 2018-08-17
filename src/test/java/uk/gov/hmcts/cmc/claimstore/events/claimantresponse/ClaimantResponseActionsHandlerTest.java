package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

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
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.DEFENDANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.ClaimantResponse.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantResponseActionsHandlerTest {
    private static final String RESPONSE_BY_CLAIMANT_TEMPLATE = "response_by_claimant";

    private ClaimantResponseActionsHandler handler;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties properties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;

    @Before
    public void setup() {
        handler = new ClaimantResponseActionsHandler(
            notificationService,
            properties
        );

        when(properties.getTemplates()).thenReturn(templates);
        when(properties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getResponseByClaimantEmailToDefendant()).thenReturn(RESPONSE_BY_CLAIMANT_TEMPLATE);
    }

    @Test
    public void sendNotificationToDefendant() {
        Claim claim = SampleClaim.getWithDefaultResponse();
        ClaimantResponseEvent claimantResponseEvent
            = new ClaimantResponseEvent(claim);

        handler.sendNotificationToDefendant(claimantResponseEvent);

        verify(notificationService, once())
            .sendMail(
                eq(DEFENDANT_EMAIL),
                eq(RESPONSE_BY_CLAIMANT_TEMPLATE),
                anyMap(),
                eq(referenceForDefendant(claim.getReferenceNumber()))
            );
    }
}
