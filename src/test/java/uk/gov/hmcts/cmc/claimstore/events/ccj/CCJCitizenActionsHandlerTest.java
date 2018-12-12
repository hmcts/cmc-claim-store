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

    private CCJCitizenActionsHandler handler;

    @Mock
    CCJNotificationService ccjNotificationService;

    @Before
    public void setup() {
        handler = new CCJCitizenActionsHandler(ccjNotificationService);
    }

    @Test
    public void notifyClaimantSuccessfully() {
        CountyCourtJudgmentEvent eventWithoutAdmission = new CountyCourtJudgmentEvent(
            SampleClaimIssuedEvent.CLAIM_WITH_DEFAULT_CCJ,
            "Bearer token here"
        );

        handler.onCountyCourtJudgment(eventWithoutAdmission);

        verify(ccjNotificationService, once()).notifyClaimantForCCJRequest(eq(eventWithoutAdmission.getClaim()));
    }

    @Test
    public void notifyClaimantAndDefendantSuccessfullyWhenByAdmission() {
        CountyCourtJudgmentEvent eventWithAdmission = new CountyCourtJudgmentEvent(
            SampleClaimIssuedEvent.CLAIM_WITH_CCJ_BY_ADMISSION,
            "Bearer token here"
        );

        handler.onCountyCourtJudgment(eventWithAdmission);

        verify(ccjNotificationService, once()).notifyClaimantForCCJRequest(eq(eventWithAdmission.getClaim()));
        verify(ccjNotificationService, once()).notifyDefendantForCCJRequested(eq(eventWithAdmission.getClaim()));
    }
}
