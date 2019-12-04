package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.mediation;

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
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MediationFailedNotificationServiceTest {

    private static final String TRANSFER_CLAIMANT = "TRANSFER_CLAIMANT";
    private static final String TRANSFER_DEFENDANT = "TRANSFER_DEFENDANT";
    private static final String OFFLINE_MEDIATION_FAILED = "OFFLINE_MEDIATION_FAILED";
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private EmailTemplates emailTemplates;
    @Mock
    private NotificationTemplates notificationTemplates;

    @Captor
    ArgumentCaptor<Map<String, String>> emailParamterCaptor;

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

        when(emailTemplates.getClaimantMediationFailureOfflineDQ())
            .thenReturn(OFFLINE_MEDIATION_FAILED);
        when(emailTemplates.getDefendantMediationFailureOfflineDQ())
            .thenReturn(OFFLINE_MEDIATION_FAILED);

        when(notificationsProperties.getFrontendBaseUrl()).thenReturn("BASELINE_URL");
    }

    @Test
    public void shouldSendTransferNotificationsWhenNonPilotCourtIsSelected() {
        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullDefence.validDefaults())
            .withClaimantResponse(SampleClaimantResponse.validRejectionWithDirectionsQuestionnaire())
            .build().toBuilder().features(Arrays.asList("admissions", "directionsQuestionnaire")).build();

        mediationFailedNotificationService.notifyParties(claim);

        verify(notificationService).sendMail(eq(claim.getSubmitterEmail()),
            eq(TRANSFER_CLAIMANT),
            any(),
            eq("transfer-claimant-mediation-unsuccessful-000CM001"));

        verify(notificationService).sendMail(eq(claim.getDefendantEmail()),
            eq(TRANSFER_DEFENDANT),
            any(),
            eq("transfer-defendant-mediation-unsuccessful-000CM001"));
    }

    @Test
    public void shouldSendotificationsWhenOfflineDQClaim() {
        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullDefence.validDefaults())
            .withClaimantResponse(SampleClaimantResponse.validRejectionWithDirectionsQuestionnaire())
            .withDirectionsQuestionnaireDeadline(LocalDate.now().plusDays(8))
            .build();

        mediationFailedNotificationService.notifyParties(claim);
        verify(notificationService).sendMail(eq(claim.getSubmitterEmail()),
            eq(OFFLINE_MEDIATION_FAILED),
            emailParamterCaptor.capture(),
            eq("offlineDQ-claimant-mediation-unsuccessful-000CM001"));

        Map<String, String> parameter = emailParamterCaptor.getValue();

        assertThat(parameter).containsKey("DQsdeadline");

        verify(notificationService).sendMail(eq(claim.getDefendantEmail()),
            eq(OFFLINE_MEDIATION_FAILED),
            any(),
            eq("offlineDQ-defendant-mediation-unsuccessful-000CM001"));
    }
}
