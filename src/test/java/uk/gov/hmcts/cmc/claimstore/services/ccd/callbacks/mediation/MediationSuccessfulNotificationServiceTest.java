package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.mediation;

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
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MediationSuccessfulNotificationServiceTest {

    private static final String MEDIATION_SUCCESSFUL = "MEDIATION_SUCCESSFUL";

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private EmailTemplates emailTemplates;
    @Mock
    private NotificationTemplates notificationTemplates;

    private MediationSuccessfulNotificationService mediationSuccessfulNotificationService;

    @Before
    public void setUp() {
        mediationSuccessfulNotificationService =
                new MediationSuccessfulNotificationService(notificationService, notificationsProperties);

        when(notificationsProperties.getTemplates()).thenReturn(notificationTemplates);
        when(notificationTemplates.getEmail()).thenReturn(emailTemplates);

        when(emailTemplates.getClaimantMediationSuccess())
                .thenReturn(MEDIATION_SUCCESSFUL);
        when(emailTemplates.getDefendantMediationSuccess())
                .thenReturn(MEDIATION_SUCCESSFUL);

        when(notificationsProperties.getFrontendBaseUrl()).thenReturn("BASELINE_URL");
    }

    @Test
    public void shouldSendNotificationToClaimantWhenMediationSuccessful() {
        Claim claim = SampleClaim.builder()
                .withResponse(SampleResponse.FullDefence.validDefaults())
                .withClaimantResponse(SampleClaimantResponse.validRejectionWithDirectionsQuestionnaire())
                .build().toBuilder().features(Arrays.asList("admissions", "directionsQuestionnaire")).build();

        mediationSuccessfulNotificationService.notifyParties(claim);

        verify(notificationService).sendMail(eq(claim.getSubmitterEmail()),
                eq(MEDIATION_SUCCESSFUL),
                any(),
                eq("to-claimant-mediation-successful"));
    }

    @Test
    public void shouldSendNotificationToDefendantWhenMediationSuccessful() {
        Claim claim = SampleClaim.builder()
                .withResponse(SampleResponse.FullDefence.validDefaults())
                .withClaimantResponse(SampleClaimantResponse.validRejectionWithDirectionsQuestionnaire())
                .build().toBuilder().features(Arrays.asList("admissions", "directionsQuestionnaire")).build();

        mediationSuccessfulNotificationService.notifyParties(claim);

        verify(notificationService).sendMail(eq(claim.getDefendantEmail()),
                eq(MEDIATION_SUCCESSFUL),
                any(),
                eq("to-defendant-mediation-successful"));
    }
}
