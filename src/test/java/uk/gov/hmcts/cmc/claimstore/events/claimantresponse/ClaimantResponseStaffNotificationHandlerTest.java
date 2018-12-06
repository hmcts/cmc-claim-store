package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.staff.StatesPaidStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantResponseStaffNotificationHandlerTest {

    private static final ClaimantResponseEvent event = new ClaimantResponseEvent(
        SampleClaim.getClaimFullDefenceStatesPaidWithAcceptation());

    private ClaimantResponseStaffNotificationHandler handler;

    @Mock
    private StatesPaidStaffNotificationService statesPaidStaffNotificationService;

    @Before
    public void setUp() {
        handler = new ClaimantResponseStaffNotificationHandler(statesPaidStaffNotificationService);
    }

    @Test
    public void notifyStaffClaimantResponseStatesPaidSubmittedFor() {
        handler.onClaimantResponse(event);

        verify(statesPaidStaffNotificationService, once())
            .notifyStaffClaimantResponseStatesPaidSubmittedFor(eq(event.getClaim()));
    }
}
