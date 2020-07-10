package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantOperationServiceTest {

    public static final String CLAIMANT_EMAIL_TEMPLATE = "Claimant Email Template";
    public static final String REPRESENTATIVE_EMAIL_TEMPLATE = "Representative Email Template";
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final String AUTHORISATION = "AUTHORISATION";
    public static final String SUBMITTER_NAME = "submitter-name";
    public static final String REPRESENTATIVE_EMAIL = "representative@Email.com";

    private ClaimantOperationService claimantOperationService;
    @Mock
    private ClaimIssuedNotificationService claimIssuedNotificationService;
    @Mock
    private NotificationsProperties notificationProperties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;
    @Mock
    private ClaimCreationEventsStatusService eventsStatusService;

    @Before
    public void before() {
        claimantOperationService = new ClaimantOperationService(claimIssuedNotificationService,
            notificationProperties, eventsStatusService);

        given(notificationProperties.getTemplates()).willReturn(templates);
        given(templates.getEmail()).willReturn(emailTemplates);
    }

    @Test
    public void shouldNotifyCitizen() {
        //given
        given(emailTemplates.getClaimantClaimIssuedWithHwf()).willReturn(CLAIMANT_EMAIL_TEMPLATE);

        //when
        claimantOperationService.notifyCitizen(CLAIM, SUBMITTER_NAME, AUTHORISATION);

        //verify
        verify(claimIssuedNotificationService).sendMail(
            eq(CLAIM),
            eq(CLAIM.getSubmitterEmail()),
            any(),
            eq(CLAIMANT_EMAIL_TEMPLATE),
            eq("claimant-issue-notification-" + CLAIM.getReferenceNumber()),
            eq(SUBMITTER_NAME)
        );
    }

    @Test
    public void shouldConfirmRepresentative() {
        //given
        given(emailTemplates.getRepresentativeClaimIssued()).willReturn(REPRESENTATIVE_EMAIL_TEMPLATE);

        //when
        claimantOperationService.confirmRepresentative(CLAIM, SUBMITTER_NAME, REPRESENTATIVE_EMAIL, AUTHORISATION);

        //verify
        verify(claimIssuedNotificationService).sendMail(
            eq(CLAIM),
            eq(REPRESENTATIVE_EMAIL),
            any(),
            eq(REPRESENTATIVE_EMAIL_TEMPLATE),
            eq("representative-issue-notification-" + CLAIM.getReferenceNumber()),
            eq(SUBMITTER_NAME)
        );
    }
}
