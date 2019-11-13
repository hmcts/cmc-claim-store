package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.mediation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MediationFailedNotificationServiceTest {

    private static final String TRANSFER_CLAIMANT = "TRANSFER_CLAIMANT";
    private static final String TRANSFER_DEFENDANT = "TRANSFER_DEFENDANT";
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private EmailTemplates emailTemplates;
    @Mock
    private NotificationTemplates notificationTemplates;

    private MediationFailedNotificationService mediationFailedNotificationService;

    @Before
    public void setUp() {
        mediationFailedNotificationService =
            new MediationFailedNotificationService(notificationService, notificationsProperties);

        when(notificationsProperties.getTemplates()).thenReturn(notificationTemplates);
        when(notificationTemplates.getEmail()).thenReturn(emailTemplates);

        when(emailTemplates.getClaimantReadyForTransfer())
            .thenReturn(TRANSFER_CLAIMANT);
        when(emailTemplates.getDefendantReadyForTransfer())
            .thenReturn(TRANSFER_DEFENDANT);
        when(notificationsProperties.getFrontendBaseUrl()).thenReturn("BASELINE_URL");
    }

    @Test
    public void shouldSendTransferNotificationsWhenNonPilotCourtIsSelected() {
        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullDefence.validDefaults())
            .withClaimantResponse(SampleClaimantResponse.validRejectionWithDirectionsQuestionnaire())
            .build();

        when(caseDetailsConverter.extractClaim(any())).thenReturn(claim);

        mediationFailedNotificationService.notifyParties(claim);

        verify(notificationService).sendMail(eq(claim.getSubmitterEmail()), eq(TRANSFER_CLAIMANT), any(), any());
        verify(notificationService).sendMail(eq(claim.getDefendantEmail()), eq(TRANSFER_DEFENDANT), any(), any());
    }
}
