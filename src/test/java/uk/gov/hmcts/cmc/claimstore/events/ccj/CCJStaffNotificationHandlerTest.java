package uk.gov.hmcts.cmc.claimstore.events.ccj;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.staff.CCJStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.InterlocutoryJudgmentStaffNotificationService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.getWithClaimantResponse;

@RunWith(MockitoJUnitRunner.class)
public class CCJStaffNotificationHandlerTest {

    private CCJStaffNotificationHandler handler;
    @Mock
    private CCJStaffNotificationService ccjStaffNotificationService;
    @Mock
    private InterlocutoryJudgmentStaffNotificationService interlocutoryJudgmentStaffNotificationService;

    @Before
    public void setup() {
        handler = new CCJStaffNotificationHandler(
            ccjStaffNotificationService,
            interlocutoryJudgmentStaffNotificationService,
            true
        );
    }

    @Test
    public void notifyStaffWhenDefaultCCJRequestSubmittedAndStaffEmailEnabled() {
        CountyCourtJudgmentEvent event = new CountyCourtJudgmentEvent(
            SampleClaimIssuedEvent.CLAIM, "Bearer token here");

        handler.onDefaultJudgmentRequestSubmitted(event);

        verify(ccjStaffNotificationService, once()).notifyStaffCCJRequestSubmitted(eq(SampleClaimIssuedEvent.CLAIM));
    }

    @Test
    public void shouldNotNotifyStaffWhenDefaultCCJRequestSubmittedAndStaffEmailDisabled() {
        handler = new CCJStaffNotificationHandler(
            ccjStaffNotificationService,
            interlocutoryJudgmentStaffNotificationService,
            false
        );
        CountyCourtJudgmentEvent event = new CountyCourtJudgmentEvent(
            SampleClaimIssuedEvent.CLAIM, "Bearer token here");

        handler.onDefaultJudgmentRequestSubmitted(event);

        verify(ccjStaffNotificationService, never()).notifyStaffCCJRequestSubmitted(eq(SampleClaimIssuedEvent.CLAIM));
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
