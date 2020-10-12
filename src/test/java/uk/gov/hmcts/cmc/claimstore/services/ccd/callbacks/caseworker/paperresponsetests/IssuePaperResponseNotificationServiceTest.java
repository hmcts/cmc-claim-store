package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperresponsetests;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.IssuePaperResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.events.operations.ClaimantOperationServiceTest.CLAIMANT_EMAIL_TEMPLATE;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@RunWith(MockitoJUnitRunner.class)
public class IssuePaperResponseNotificationServiceTest {

    private IssuePaperResponseNotificationService issuePaperResponseNotificationService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private NotificationTemplates notificationTemplates;
    @Mock
    private EmailTemplates emailTemplates;

    @Before
    public void setUp() {
        issuePaperResponseNotificationService = new IssuePaperResponseNotificationService(
                notificationService,
                notificationsProperties
        );
        given(notificationsProperties.getFrontendBaseUrl()).willReturn(FRONTEND_BASE_URL);
        given(notificationsProperties.getTemplates()).willReturn(notificationTemplates);
        given(notificationTemplates.getEmail()).willReturn(emailTemplates);
        given(emailTemplates.getDefendantAskedToRespondByPost()).willReturn(CLAIMANT_EMAIL_TEMPLATE);
    }

    @Test
    public void shouldSendEmailToClaimantUsingPredefinedTemplate() {
        Claim claim = SampleClaim.builder().withSubmitterEmail("claimant@mail.com").build();
        Map<String, String> expectedParams = ImmutableMap.of(
                "claimReferenceNumber", claim.getReferenceNumber(),
                "claimantName", claim.getClaimData().getClaimant().getName(),
                "defendantName", claim.getClaimData().getDefendant().getName(),
                "frontendBaseUrl", FRONTEND_BASE_URL,
                "responseDeadline", formatDate(claim.getResponseDeadline())
        );

        issuePaperResponseNotificationService.notifyClaimant(claim);

        verify(notificationService).sendMail(
                eq(claim.getSubmitterEmail()),
                eq(CLAIMANT_EMAIL_TEMPLATE),
                eq(expectedParams),
                eq(NotificationReferenceBuilder.IssuePaperDefence
                        .notifyClaimantPaperResponseFormsSentToDefendant(claim.getReferenceNumber(), 
                                "claimant")));

    }
}
