package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.cmc.claimstore.services.ScheduledStateTransitionService;
import uk.gov.hmcts.cmc.claimstore.services.statetransition.StateTransitions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StayClaimJobTest {

    @Mock
    private ScheduledStateTransitionService scheduledStateTransitionService;

    private StayClaimJob stayClaimJob;

    private final String cronExpression = "0 * * * * *";

    @Before
    public void setup() {
        stayClaimJob = new StayClaimJob();
        stayClaimJob.setCronExpression(cronExpression);
        stayClaimJob.setScheduledStateTransitionService(scheduledStateTransitionService);
    }

    @Test
    public void executeShouldTriggerIntentionToProceed() throws Exception {
        stayClaimJob.execute(null);

        verify(scheduledStateTransitionService).stateChangeTriggered(eq(StateTransitions.STAY_CLAIM));
    }

    @Test(expected = JobExecutionException.class)
    public void shouldThrowJobExecutionException() throws Exception {
        doThrow(new RuntimeException()).when(scheduledStateTransitionService).stateChangeTriggered(any());

        stayClaimJob.execute(null);
    }
}
