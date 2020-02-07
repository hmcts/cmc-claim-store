package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectionStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.StatesPaidStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantResponseStaffNotificationHandlerTest {

    private ClaimantResponseStaffNotificationHandler handler;

    @Mock
    private StatesPaidStaffNotificationService statesPaidStaffNotificationService;

    @Mock
    private ClaimantRejectionStaffNotificationService claimantRejectionStaffNotificationService;

    private final String authorisation = "Bearer authorisation";

    @Before
    public void setUp() {
        handler = new ClaimantResponseStaffNotificationHandler(
            statesPaidStaffNotificationService,
            claimantRejectionStaffNotificationService
        );
    }

    @Test
    public void notifyStaffClaimantResponseStatesPaidSubmittedFor() {
        ClaimantResponseEvent event = new ClaimantResponseEvent(
            SampleClaim.getClaimFullDefenceStatesPaidWithAcceptation(), authorisation);
        handler.onClaimantResponse(event);

        verify(statesPaidStaffNotificationService, once())
            .notifyStaffClaimantResponseStatesPaidSubmittedFor(eq(event.getClaim()));
    }

    @Test
    public void notifyStaffClaimantResponseRejectedPartAdmission() {
        ClaimantResponseEvent event = new ClaimantResponseEvent(
            SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation(), authorisation
        );
        handler.onClaimantResponse(event);

        verify(claimantRejectionStaffNotificationService, once())
            .notifyStaffClaimantRejectPartAdmission(eq(event.getClaim()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionWhenResponseNotPresent() {
        ClaimantResponseEvent event = new ClaimantResponseEvent(SampleClaim.builder().build(), authorisation);
        handler.onClaimantResponse(event);

        verifyNoInteractions(statesPaidStaffNotificationService, claimantRejectionStaffNotificationService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenClaimantResponseNotPresent() {
        ClaimantResponseEvent event = new ClaimantResponseEvent(SampleClaim.builder().build(), authorisation);

        handler.notifyStaffWithClaimantsIntentionToProceed(event);

        verifyNoInteractions(statesPaidStaffNotificationService, claimantRejectionStaffNotificationService);
    }

    public void shouldNotifyStaffWhenClaimantIntendsToProceed() {
        Claim claim = Claim.builder()
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .claimantResponse(
                SampleClaimantResponse.ClaimantResponseRejection.builder()
                    .buildRejectionWithDirectionsQuestionnaire()
            )
            .build();
        ClaimantResponseEvent event = new ClaimantResponseEvent(claim, authorisation);

        handler.notifyStaffWithClaimantsIntentionToProceed(event);

        verify(claimantRejectionStaffNotificationService, once())
            .notifyStaffClaimantRejectPartAdmission(eq(event.getClaim()));
    }
}
