package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.TransferContent;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_EMAIL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.SUBMITTER_EMAIL;

@ExtendWith(MockitoExtension.class)
class TransferCaseNotificationsServiceTest {

    private static final String CASE_TRANSFERRED_TEMPLATE = "CASE_TRANSFERRED_TEMPLATE";
    private static final String TRANSFER_COURT_NAME = "Bristol";

    @InjectMocks
    private TransferCaseNotificationsService transferCaseNotificationsService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    private Claim claim;

    @BeforeEach
    public void beforeEach() {

        NotificationTemplates notificationTemplates = mock(NotificationTemplates.class);
        EmailTemplates emailTemplates = mock(EmailTemplates.class);

        when(emailTemplates.getCaseTransferred()).thenReturn(CASE_TRANSFERRED_TEMPLATE);
        when(notificationTemplates.getEmail()).thenReturn(emailTemplates);
        when(notificationsProperties.getTemplates()).thenReturn(notificationTemplates);
        when(notificationsProperties.getFrontendBaseUrl()).thenReturn(FRONTEND_BASE_URL);

        claim = SampleClaim.builder()
            .withDefendantEmail(DEFENDANT_EMAIL)
            .withTransferContent(TransferContent.builder()
                .hearingCourtName(TRANSFER_COURT_NAME)
                .build())
            .build();
    }

    @Test
    void shouldSendClaimUpdatedEmailToClaimant() {

        transferCaseNotificationsService.sendClaimUpdatedEmailToClaimant(claim);

        thenEmailSent(SUBMITTER_EMAIL, "to-claimant-case-transferred-000MC001");
    }

    @Test
    void shouldSendClaimUpdatedEmailToDefendant() {
        transferCaseNotificationsService.sendClaimUpdatedEmailToDefendant(claim);

        thenEmailSent(DEFENDANT_EMAIL, "to-defendant-case-transferred-000MC001");
    }

    private void thenEmailSent(String recipientEmail, String reference) {

        Map<String, String> expectedParams = Map.of(
            "claimReferenceNumber", claim.getReferenceNumber(),
            "claimantName", claim.getClaimData().getClaimant().getName(),
            "defendantName", claim.getClaimData().getDefendant().getName(),
            "frontendBaseUrl", FRONTEND_BASE_URL,
            "externalId", claim.getExternalId(),
            "courtName", TRANSFER_COURT_NAME
        );

        verify(notificationService).sendMail(
            eq(recipientEmail),
            eq(CASE_TRANSFERRED_TEMPLATE),
            eq(expectedParams),
            eq(reference));
    }
}
