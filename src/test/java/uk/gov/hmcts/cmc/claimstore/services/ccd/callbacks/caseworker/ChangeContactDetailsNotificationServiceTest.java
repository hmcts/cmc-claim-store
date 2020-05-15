package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.events.operations.ClaimantOperationServiceTest.CLAIMANT_EMAIL_TEMPLATE;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@RunWith(MockitoJUnitRunner.class)
public class ChangeContactDetailsNotificationServiceTest {

    public static final String DEFENDANT_EMAIL_TEMPLATE = "Defendant Email Template";

    private ChangeContactDetailsNotificationService changeContactDetailsNotificationService;
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
        changeContactDetailsNotificationService = new ChangeContactDetailsNotificationService(
            notificationService,
            notificationsProperties
        );

        given(notificationsProperties.getFrontendBaseUrl()).willReturn(FRONTEND_BASE_URL);
        given(notificationsProperties.getTemplates()).willReturn(notificationTemplates);
        given(notificationTemplates.getEmail()).willReturn(emailTemplates);
        given(emailTemplates.getDefendantContactDetailsChanged()).willReturn(DEFENDANT_EMAIL_TEMPLATE);
        given(emailTemplates.getClaimantContactDetailsChanged()).willReturn(CLAIMANT_EMAIL_TEMPLATE);
    }

    @Test
    public void shouldSendEmailToClaimantUsingPredefinedTemplate() {
        Claim claim = SampleClaim.builder().withSubmitterEmail("claimant@mail.com").build();
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase = SampleData.addContactChangePartyDefendant(ccdCase);
        Map<String, String> expectedParams = ImmutableMap.of(
            "claimReferenceNumber", claim.getReferenceNumber(),
            "claimantName", claim.getClaimData().getClaimant().getName(),
            "defendantName", claim.getClaimData().getDefendant().getName(),
            "frontendBaseUrl", FRONTEND_BASE_URL,
            "externalId", claim.getExternalId()
        );

        changeContactDetailsNotificationService.sendEmailToRightRecipient(ccdCase, claim);

        verify(notificationService).sendMail(
            eq(claim.getSubmitterEmail()),
            eq(DEFENDANT_EMAIL_TEMPLATE),
            eq(expectedParams),
            eq(NotificationReferenceBuilder.ContactDetailsChanged
                .referenceForContactChanges(claim.getReferenceNumber(), "claimant")));

    }

    @Test
    public void shouldSendEmailToDefendantUsingPredefinedTemplate() {
        Claim claim = SampleClaim.builder().withDefendantEmail("defendant@mail.com").build();
        CCDCase ccdCase = SampleData.getCCDCitizenCase(Collections.emptyList());
        ccdCase = SampleData.addContactChangePartyClaimant(ccdCase);
        Map<String, String> expectedParams = ImmutableMap.of(
            "claimReferenceNumber", claim.getReferenceNumber(),
            "claimantName", claim.getClaimData().getClaimant().getName(),
            "defendantName", claim.getClaimData().getDefendant().getName(),
            "frontendBaseUrl", FRONTEND_BASE_URL,
            "externalId", claim.getExternalId()
        );

        changeContactDetailsNotificationService.sendEmailToRightRecipient(ccdCase, claim);

        verify(notificationService).sendMail(
            eq(claim.getDefendantEmail()),
            eq(CLAIMANT_EMAIL_TEMPLATE),
            eq(expectedParams),
            eq(NotificationReferenceBuilder.ContactDetailsChanged
                .referenceForContactChanges(claim.getReferenceNumber(), "defendant")));
    }
}
