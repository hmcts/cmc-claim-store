package uk.gov.hmcts.cmc.claimstore.events.paidinfull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.services.staff.PaidInFullStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@ExtendWith(MockitoExtension.class)
public class PaidInFullStaffNotificationHandlerTest {

    private static final PaidInFullEvent event = new PaidInFullEvent(SampleClaim.getDefault());

    private PaidInFullStaffNotificationHandler handler;

    @Mock
    PaidInFullStaffNotificationService paidInFullStaffNotificationService;

    @BeforeEach
    public void setup() {
        handler = new PaidInFullStaffNotificationHandler(paidInFullStaffNotificationService);
    }

    @Test
    public void notifyStaffCCJRequestSubmitted() {
        handler.onPaidInFullEvent(event);

        verify(paidInFullStaffNotificationService, once()).notifyPaidInFull(eq(event.getClaim()));
    }
}
