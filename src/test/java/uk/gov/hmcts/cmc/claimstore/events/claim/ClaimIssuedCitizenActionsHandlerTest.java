package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIM;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.CLAIMANT_EMAIL;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.PIN;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.SUBMITTER_NAME;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.getClaimWithNoDefendantEmail;

@RunWith(MockitoJUnitRunner.class)
public class ClaimIssuedCitizenActionsHandlerTest {
    private static final String CLAIMANT_CLAIM_ISSUED_TEMPLATE = "claimantClaimIssued";
    private static final String DEFENDANT_CLAIM_ISSUED_TEMPLATE = "defendantClaimIssued";
    private static final String AUTHORISATION = "Bearer: aaa";

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
    public void sendNotificationsSendsNotificationsToClaimantAndDefendant() {

        CitizenClaimIssuedEvent claimIssuedEvent
            = new CitizenClaimIssuedEvent(CLAIM, PIN, SUBMITTER_NAME, AUTHORISATION);

        claimIssuedCitizenActionsHandler.sendClaimantNotification(claimIssuedEvent);
        claimIssuedCitizenActionsHandler.sendDefendantNotification(claimIssuedEvent);

        verify(claimIssuedNotificationService, once())
            .sendMail(CLAIM,
                CLAIMANT_EMAIL,
                null,
                CLAIMANT_CLAIM_ISSUED_TEMPLATE,
                "claimant-issue-notification-" + claimIssuedEvent.getClaim().getReferenceNumber(),
                SUBMITTER_NAME);

        verify(claimIssuedNotificationService, once())
            .sendMail(CLAIM,
                SampleClaimIssuedEvent.DEFENDANT_EMAIL,
                PIN,
                DEFENDANT_CLAIM_ISSUED_TEMPLATE,
                "defendant-issue-notification-" + claimIssuedEvent.getClaim().getReferenceNumber(),
                SUBMITTER_NAME
            );
    }

    @Test
    public void sendNotificationsSendsNotificationToClaimantOnly() {

        Claim claimNoDefendantEmail = getClaimWithNoDefendantEmail();

        CitizenClaimIssuedEvent claimIssuedEvent
            = new CitizenClaimIssuedEvent(claimNoDefendantEmail, PIN, SUBMITTER_NAME, AUTHORISATION);

        claimIssuedCitizenActionsHandler.sendClaimantNotification(claimIssuedEvent);
        claimIssuedCitizenActionsHandler.sendDefendantNotification(claimIssuedEvent);

        verify(claimIssuedNotificationService, once())
            .sendMail(claimNoDefendantEmail,
                CLAIMANT_EMAIL,
                null,
                CLAIMANT_CLAIM_ISSUED_TEMPLATE,
                "claimant-issue-notification-" + claimIssuedEvent.getClaim().getReferenceNumber(),
                SUBMITTER_NAME
            );

        verify(claimIssuedNotificationService, never())
            .sendMail(claimNoDefendantEmail,
                SampleClaimIssuedEvent.DEFENDANT_EMAIL,
                PIN,
                DEFENDANT_CLAIM_ISSUED_TEMPLATE,
                "defendant-issue-notification-" + claimIssuedEvent.getClaim().getReferenceNumber(),
                SUBMITTER_NAME);
    }
}
