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

    private CCJStaffNotificationHandler handler;

    @Mock
    CCJStaffNotificationService ccjStaffNotificationService;

    @Before
    public void setup() {
        handler = new CCJStaffNotificationHandler(ccjStaffNotificationService);
    }

    @Test
    public void notifyStaffDefaultCCJRequestSubmitted() {
        CountyCourtJudgmentEvent event = new CountyCourtJudgmentEvent(
            SampleClaimIssuedEvent.CLAIM, "Bearer token here");

        handler.onDefaultJudgmentRequestSubmitted(event);

        verify(ccjStaffNotificationService, once()).notifyStaffCCJRequestSubmitted(eq(SampleClaimIssuedEvent.CLAIM));
    }

    @Test
    public void notifyStaffCCJRequestByAdmissionSubmitted() {
        CountyCourtJudgmentEvent event = new CountyCourtJudgmentEvent(
            SampleClaimIssuedEvent.CLAIM, "Bearer token here");

        handler.onDefaultJudgmentRequestSubmitted(event);

        verify(ccjStaffNotificationService, once()).notifyStaffCCJRequestSubmitted(eq(SampleClaimIssuedEvent.CLAIM));
    }
}
