package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

public class ChangeContactDetailsNotificationServiceTest {

    private static final String reference = "to-s%-contact-details-change-s%";
    ChangeContactDetailsNotificationService changeContactDetailsNotificationService;
    @Mock
    CaseDetailsConverter caseDetailsConverter;
    @Mock
    NotificationService notificationService;
    @Mock
    NotificationsProperties notificationsProperties;

    @BeforeEach
    void setUp() {
        changeContactDetailsNotificationService = new ChangeContactDetailsNotificationService(
                caseDetailsConverter,
                notificationService,
                notificationsProperties
        );

    }

    @Test
    public void shouldSendEmailToClaimantUsingPredefinedTemplate() {
        Claim claim = SampleClaim.builder().build();
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
                eq(SampleClaim.SUBMITTER_EMAIL),
                eq("defendantContactDetailsChanged"),
                eq(expectedParams),
                eq(String.format(reference, "claimant", claim.getReferenceNumber())));
    }

    @Test
    public void shouldSendEmailToDefendantUsingPredefinedTemplate() {
        Claim claim = SampleClaim.builder().build();
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
                eq(SampleClaim.DEFENDANT_EMAIL),
                eq("claimantContactDetailsChanged"),
                eq(expectedParams),
                eq(String.format(reference, "defendant", claim.getReferenceNumber())));
    }
}
