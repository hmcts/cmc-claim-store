package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CCJNotificationServiceTest extends BaseNotificationServiceTest {

    @Mock
    private NotificationClient notificationClient;

    private CCJNotificationService ccjNotificationService;

    @Before
    public void setup() {
        ccjNotificationService = new CCJNotificationService(
            notificationClient,
            properties
        );

        when(templates.getEmail()).thenReturn(emailTemplates);
        when(properties.getTemplates()).thenReturn(templates);
        when(emailTemplates.getClaimantCCJRequested()).thenReturn(CLAIMANT_CCJ_REQUESTED_TEMPLATE);
    }

    @Test
    public void notifyClaimantShouldCallNotify() throws Exception {
        Claim claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        ccjNotificationService.notifyClaimantForCCJRequest(claim);

        verify(notificationClient)
            .sendEmail(
                eq(CLAIMANT_CCJ_REQUESTED_TEMPLATE),
                eq(claim.getSubmitterEmail()),
                anyMap(),
                eq(NotificationReferenceBuilder.CCJRequested.referenceForClaimant(claim.getReferenceNumber()))
            );
    }
}
