package uk.gov.hmcts.cmc.claimstore.events;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleClaim.getClaimWithNoDefendantEmail;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.SUBMITTER_NAME;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class ClaimIssuedCitizenActionsHandlerTest {
    private static final String CLAIMANT_CLAIM_ISSUED_TEMPLATE = "claimantClaimIssued";
    private static final String DEFENDANT_CLAIM_ISSUED_TEMPLATE = "defendantClaimIssued";

    private ClaimIssuedCitizenActionsHandler claimIssuedCitizenActionsHandler;
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
        claimIssuedCitizenActionsHandler = new ClaimIssuedCitizenActionsHandler(
            claimIssuedNotificationService,
            properties
        );
        when(properties.getTemplates()).thenReturn(templates);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getClaimantClaimIssued()).thenReturn(CLAIMANT_CLAIM_ISSUED_TEMPLATE);
        when(emailTemplates.getDefendantClaimIssued()).thenReturn(DEFENDANT_CLAIM_ISSUED_TEMPLATE);
    }

    @Test
    public void sendNotificationsSendsNotificationsToClaimantAndDefendant() throws NotificationClientException {

        final ClaimIssuedEvent claimIssuedEvent
            = new ClaimIssuedEvent(SampleClaimIssuedEvent.CLAIM, SampleClaimIssuedEvent.PIN, SUBMITTER_NAME);

        claimIssuedCitizenActionsHandler.sendClaimantNotification(claimIssuedEvent);
        claimIssuedCitizenActionsHandler.sendDefendantNotification(claimIssuedEvent);

        verify(claimIssuedNotificationService, once()).sendMail(SampleClaimIssuedEvent.CLAIM,
            SampleClaimIssuedEvent.CLAIMANT_EMAIL, Optional.empty(), CLAIMANT_CLAIM_ISSUED_TEMPLATE,
            "claimant-issue-notification-" + claimIssuedEvent.getClaim().getReferenceNumber(),
            SUBMITTER_NAME);

        verify(claimIssuedNotificationService, once()).sendMail(SampleClaimIssuedEvent.CLAIM,
            SampleClaimIssuedEvent.DEFENDANT_EMAIL, Optional.of(SampleClaimIssuedEvent.PIN),
            DEFENDANT_CLAIM_ISSUED_TEMPLATE,
            "defendant-issue-notification-" + claimIssuedEvent.getClaim().getReferenceNumber(),
            SUBMITTER_NAME);
    }

    @Test
    public void sendNotificationsSendsNotificationToClaimantOnly() throws NotificationClientException {

        Claim claimNoDefendantEmail = getClaimWithNoDefendantEmail();

        ClaimIssuedEvent claimIssuedEvent
            = new ClaimIssuedEvent(claimNoDefendantEmail, SampleClaimIssuedEvent.PIN, SUBMITTER_NAME);

        claimIssuedCitizenActionsHandler.sendClaimantNotification(claimIssuedEvent);
        claimIssuedCitizenActionsHandler.sendDefendantNotification(claimIssuedEvent);

        verify(claimIssuedNotificationService, once()).sendMail(claimNoDefendantEmail,
            SampleClaimIssuedEvent.CLAIMANT_EMAIL, Optional.empty(), CLAIMANT_CLAIM_ISSUED_TEMPLATE,
            "claimant-issue-notification-" + claimIssuedEvent.getClaim().getReferenceNumber(),
            SUBMITTER_NAME);

        verify(claimIssuedNotificationService, never()).sendMail(claimNoDefendantEmail,
            SampleClaimIssuedEvent.DEFENDANT_EMAIL, Optional.of(SampleClaimIssuedEvent.PIN),
            DEFENDANT_CLAIM_ISSUED_TEMPLATE,
            "defendant-issue-notification-" + claimIssuedEvent.getClaim().getReferenceNumber(),
            SUBMITTER_NAME);
    }
}
