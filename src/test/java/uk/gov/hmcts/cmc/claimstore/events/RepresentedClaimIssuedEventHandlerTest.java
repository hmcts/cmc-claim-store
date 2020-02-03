package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentativeConfirmationHandler;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIM;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIMANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.SUBMITTER_NAME;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class RepresentedClaimIssuedEventHandlerTest {
    private static final String REPRESENTATIVE_CLAIM_ISSUED_TEMPLATE = "representativeClaimIssued";
    private static final String AUTHORISATION = "Bearer: aaa";

    private RepresentativeConfirmationHandler representativeConfirmationHandler;
    @Mock
    private ClaimIssuedNotificationService claimIssuedNotificationService;

    @Mock
    private NotificationsProperties properties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;

    @Before
    public void setup() {
        representativeConfirmationHandler = new RepresentativeConfirmationHandler(
            claimIssuedNotificationService,
            properties
        );
        when(properties.getTemplates()).thenReturn(templates);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getRepresentativeClaimIssued()).thenReturn(REPRESENTATIVE_CLAIM_ISSUED_TEMPLATE);
    }

    @Test
    public void sendNotificationsSendsNotificationsToRepresentative() {

        RepresentedClaimIssuedEvent representedClaimIssuedEvent
            = new RepresentedClaimIssuedEvent(CLAIM, SUBMITTER_NAME, AUTHORISATION);

        representativeConfirmationHandler.sendConfirmation(representedClaimIssuedEvent);

        verify(claimIssuedNotificationService, once()).sendMail(CLAIM,
            CLAIMANT_EMAIL, null, REPRESENTATIVE_CLAIM_ISSUED_TEMPLATE,
            "representative-issue-notification-" + representedClaimIssuedEvent.getClaim().getReferenceNumber(),
            SUBMITTER_NAME);
    }
}
