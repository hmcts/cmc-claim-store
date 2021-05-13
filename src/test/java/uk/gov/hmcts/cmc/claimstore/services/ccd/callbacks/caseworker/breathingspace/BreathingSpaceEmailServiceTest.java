package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.breathingspace;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace.BreathingSpaceEmailService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;

@RunWith(MockitoJUnitRunner.class)
public class BreathingSpaceEmailServiceTest {

    public static final String DEFENDANT_EMAIL_TEMPLATE = "Defendant Email Template";
    public static final String CLAIMANT_EMAIL_TEMPLATE = "Claimant Email Template";

    private BreathingSpaceEmailService breathingSpaceEmailService;
    @Mock
    private NotificationService notificationService;

    @Before
    public void setUp() {
        breathingSpaceEmailService = new BreathingSpaceEmailService(notificationService);
    }

    @Test
    public void shouldSendEmailToClaimantUsingPredefinedTemplate() {
        Claim claim = SampleClaim.builder().withDefendantEmail("defendant@mail.com").build();
        Map<String, String> expectedParams = ImmutableMap.of(
            CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber(),
            CLAIMANT_NAME, claim.getClaimData().getClaimant().getName(),
            DEFENDANT_NAME, claim.getClaimData().getDefendant().getName()
        );

        breathingSpaceEmailService.sendEmailNotificationToDefendant(claim, DEFENDANT_EMAIL_TEMPLATE);

        verify(notificationService).sendMail(
            claim.getDefendantEmail(),
            DEFENDANT_EMAIL_TEMPLATE,
            expectedParams,
            referenceForDefendant(claim.getReferenceNumber()));

    }

    @Test
    public void shouldSendEmailToDefendantUsingPredefinedTemplate() {
        Claim claim = SampleClaim.builder().withSubmitterEmail("claimant@mail.com").build();
        Map<String, String> expectedParams = ImmutableMap.of(
            CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber(),
            CLAIMANT_NAME, claim.getClaimData().getClaimant().getName(),
            DEFENDANT_NAME, claim.getClaimData().getDefendant().getName()
        );

        breathingSpaceEmailService.sendNotificationToClaimant(claim, CLAIMANT_EMAIL_TEMPLATE);

        verify(notificationService).sendMail(
            claim.getSubmitterEmail(),
            CLAIMANT_EMAIL_TEMPLATE,
            expectedParams,
            referenceForClaimant(claim.getReferenceNumber()));
    }
}
