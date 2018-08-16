package uk.gov.hmcts.cmc.claimstore.events.ccj;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.services.notifications.CCJNotificationService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class CCJCitizenActionsHandlerTest {

    private static final CountyCourtJudgmentEvent EVENT = new CountyCourtJudgmentEvent(
        SampleClaimIssuedEvent.CLAIM, "Bearer token here", false
    );
    private CCJCitizenActionsHandler handler;

    @Mock
    CCJNotificationService ccjNotificationService;

    @Before
    public void setup() {
        handler = new CCJCitizenActionsHandler(ccjNotificationService);
    }

    @Test
    public void notifyClaimantSuccessfully() {
        handler.sendNotification(EVENT);

        verify(ccjNotificationService, once()).notifyClaimantForCCJRequest(eq(EVENT.getClaim()));
    }
}
