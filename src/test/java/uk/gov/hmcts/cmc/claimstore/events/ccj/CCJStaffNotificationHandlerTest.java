package uk.gov.hmcts.cmc.claimstore.events.ccj;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.staff.CCJStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.InterlocutoryJudgmentStaffNotificationService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.getDefault;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.getWithClaimantResponse;

@RunWith(MockitoJUnitRunner.class)
public class CCJStaffNotificationHandlerTest {

    private static final CountyCourtJudgmentEvent EVENT = new CountyCourtJudgmentEvent(
        getDefault(), "Bearer token here", false
    );
    private CCJStaffNotificationHandler handler;

    @Mock
    CCJStaffNotificationService ccjStaffNotificationService;

    @Mock
    InterlocutoryJudgmentStaffNotificationService interlocutoryJudgmentStaffNotificationService;

    @Before
    public void setup() {
        handler = new CCJStaffNotificationHandler(
            ccjStaffNotificationService,
            interlocutoryJudgmentStaffNotificationService
        );
    }

    @Test
    public void notifyStaffCCJRequestSubmitted() {
        handler.onDefaultJudgmentRequestSubmitted(EVENT);

        verify(ccjStaffNotificationService, once()).notifyStaffCCJRequestSubmitted(eq(EVENT.getClaim()));

    }

    @Test
    public void notifyInterlocutoryJudgmentSubmitted() {
        InterlocutoryJudgmentEvent interlocutoryJudgmentEvent = new InterlocutoryJudgmentEvent(
            getWithClaimantResponse()
        );
        handler.onInterlocutoryJudgmentEvent(interlocutoryJudgmentEvent);

        verify(interlocutoryJudgmentStaffNotificationService, once())
            .notifyStaffInterlocutoryJudgmentSubmitted(eq(interlocutoryJudgmentEvent.getClaim()));
    }
}
