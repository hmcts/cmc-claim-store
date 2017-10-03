package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotificationException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleTheirDetails;
import uk.gov.service.notify.NotificationClient;

import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class CCJRequestedNotificationServiceTest extends BaseNotificationServiceTest {

    @Mock
    private NotificationClient notificationClient;

    private CCJRequestedNotificationService ccjRequestedNotificationService;

    @Before
    public void setup() {
        ccjRequestedNotificationService = new CCJRequestedNotificationService(
            notificationClient,
            properties
        );

        when(templates.getEmail()).thenReturn(emailTemplates);
        when(properties.getTemplates()).thenReturn(templates);
        when(emailTemplates.getClaimantCCJRequested()).thenReturn(CLAIMANT_CCJ_REQUESTED_TEMPLATE);
        when(emailTemplates.getDefendantCCJRequested()).thenReturn(DEFENDANT_CCJ_REQUESTED_TEMPLATE);
    }

    @Test
    public void notifyClaimantShouldCallNotify() throws Exception {
        Claim claim = SampleClaim.getDefault();

        ccjRequestedNotificationService.notifyClaimant(claim);

        verify(notificationClient, once())
            .sendEmail(
                eq(CLAIMANT_CCJ_REQUESTED_TEMPLATE),
                eq(claim.getSubmitterEmail()),
                any(HashMap.class),
                eq(NotificationReferenceBuilder.CCJRequested.referenceForClaimant(claim.getReferenceNumber()))
            );
    }

    @Test(expected = NotificationException.class)
    public void notifyDefendantShouldThrowNotificationExceptionWhenDefendantEmailIsNotProvided() throws Exception {

        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withDefendants(
                        Arrays.asList(SampleTheirDetails.builder().withEmail(null).individualDetails())
                    ).build()
            ).build();

        ccjRequestedNotificationService.notifyDefendant(claim);
    }

    @Test
    public void notifyDefendantShouldCallNotify() throws Exception {

        final String email = "My@example.com";

        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withDefendants(
                        Arrays.asList(SampleTheirDetails.builder().withEmail(email).individualDetails())
                    ).build()
            ).build();

        ccjRequestedNotificationService.notifyDefendant(claim);

        verify(notificationClient, once())
            .sendEmail(
                eq(DEFENDANT_CCJ_REQUESTED_TEMPLATE),
                eq(email),
                any(HashMap.class),
                eq(NotificationReferenceBuilder.CCJRequested.referenceForDefendant(claim.getReferenceNumber()))
            );
    }
}
