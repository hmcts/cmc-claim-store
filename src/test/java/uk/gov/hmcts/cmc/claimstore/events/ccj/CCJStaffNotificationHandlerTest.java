package uk.gov.hmcts.cmc.claimstore.events.ccj;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.staff.CCJStaffNotificationService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class CCJStaffNotificationHandlerTest {

    private static final CountyCourtJudgmentEvent EVENT = new CountyCourtJudgmentEvent(
        SampleClaimIssuedEvent.CLAIM, "Bearer token here", false);
    private CCJStaffNotificationHandler handler;

    @Mock
    CCJStaffNotificationService ccjStaffNotificationService;

    @Before
    public void setup() {
        handler = new CCJStaffNotificationHandler(ccjStaffNotificationService);
    }

    @Test
    public void notifyStaffCCJRequestSubmitted() {
        handler.onDefaultJudgmentRequestSubmitted(EVENT);

        verify(ccjStaffNotificationService, once()).notifyStaffCCJRequestSubmitted(eq(SampleClaimIssuedEvent.CLAIM));

    }
}
