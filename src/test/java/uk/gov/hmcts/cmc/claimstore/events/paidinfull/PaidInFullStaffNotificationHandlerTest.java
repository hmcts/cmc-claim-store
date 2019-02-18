package uk.gov.hmcts.cmc.claimstore.events.paidinfull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.staff.PaidInFullStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class PaidInFullStaffNotificationHandlerTest {

    private static final PaidInFullEvent event = new PaidInFullEvent(SampleClaim.getDefault());

    private PaidInFullStaffNotificationHandler handler;

    @Mock
    PaidInFullStaffNotificationService paidInFullStaffNotificationService;

    @Before
    public void setup() {
        handler = new PaidInFullStaffNotificationHandler(paidInFullStaffNotificationService);
    }

    @Test
    public void notifyStaffCCJRequestSubmitted() {
        handler.onPaidInFullEvent(event);

        verify(paidInFullStaffNotificationService, once()).notifyPaidInFull(eq(event.getClaim()));
    }
}
