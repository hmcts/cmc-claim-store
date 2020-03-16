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
public class WaitingTransferJobTest {

    @Mock
    private ScheduledStateTransitionService scheduledStateTransitionService;

    private WaitingTransferJob intentionToProceedJob;

    private final String cronExpression = "0 * * * * *";

    @Before
    public void setup() {
        intentionToProceedJob = new WaitingTransferJob();
        intentionToProceedJob.setCronExpression(cronExpression);
        intentionToProceedJob.setScheduledStateTransitionService(scheduledStateTransitionService);
    }

    @Test
    public void executeShouldTriggerReadyForTransfer() throws Exception {
        intentionToProceedJob.execute(null);

        verify(scheduledStateTransitionService).stateChangeTriggered(eq(StateTransitions.WAITING_TRANSFER));
    }

    @Test(expected = JobExecutionException.class)
    public void shouldThrowJobExecutionException() throws Exception {
        doThrow(new RuntimeException()).when(scheduledStateTransitionService).stateChangeTriggered(any());

        intentionToProceedJob.execute(null);
    }
}
