package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationToDefendantService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectOrgPaymentPlanStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantResponseActionHandlerTest {
    private final String authorisation = "Bearer authorisation";

    private ClaimantResponseActionsHandler handler;

    @Mock
    private NotificationToDefendantService notificationService;
    @Mock
    private ClaimantRejectOrgPaymentPlanStaffNotificationService claimantRejectOrgPaymentPlanStaffNotificationService;

    @Before
    public void setUp() {
        handler = new ClaimantResponseActionsHandler(
            notificationService,
            claimantRejectOrgPaymentPlanStaffNotificationService
        );
    }

    @Test
    public void shouldNotifyDefendantOfIntentToProceed() {
        Claim claim = SampleClaim.builder()
            .withClaimantRespondedAt(LocalDateTime.now())
            .withClaimantResponse(SampleClaimantResponse.validRejectionWithDirectionsQuestionnaire())
            .build();
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        handler.notifyDefendantOfIntentToProceed(event);

        verify(notificationService, once())
            .notifyDefendantOfClaimantResponse(eq(event.getClaim()));
    }

    @Test
    public void shouldNotNotifyDefendantOfIntentToProceedWhenClaimantDoesNotIntentToProceed() {
        Claim claim = SampleClaim.builder()
            .withClaimantRespondedAt(LocalDateTime.now())
            .withClaimantResponse(SampleClaimantResponse.validDefaultRejection())
            .build();
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);
        handler.notifyDefendantOfIntentToProceed(event);

        verify(notificationService, never())
            .notifyDefendantOfClaimantResponse(event.getClaim());
    }
}
