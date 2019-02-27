package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectionStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.StatesPaidStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantResponseStaffNotificationHandlerTest {

    private ClaimantResponseStaffNotificationHandler handler;

    @Mock
    private StatesPaidStaffNotificationService statesPaidStaffNotificationService;

    @Mock
    private ClaimantRejectionStaffNotificationService claimantRejectionStaffNotificationService;

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
            SampleClaim.getClaimFullDefenceStatesPaidWithAcceptation());
        handler.onClaimantResponse(event);

        verify(statesPaidStaffNotificationService, once())
            .notifyStaffClaimantResponseStatesPaidSubmittedFor(eq(event.getClaim()));
    }

    @Test
    public void notifyStaffClaimantResponseRejectedPartAdmission() {
        ClaimantResponseEvent event = new ClaimantResponseEvent(
            SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation()
        );
        handler.onClaimantResponse(event);

        verify(claimantRejectionStaffNotificationService, once())
            .notifyStaffClaimantRejectPartAdmission(eq(event.getClaim()));
    }
}
