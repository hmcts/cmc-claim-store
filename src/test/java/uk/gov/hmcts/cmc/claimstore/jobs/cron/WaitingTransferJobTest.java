package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.cmc.claimstore.services.ScheduledStateTransitionService;
import uk.gov.hmcts.cmc.claimstore.services.statetransition.StateTransitions;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WaitingTransferJobTest {

    @Mock
    private ScheduledStateTransitionService scheduledStateTransitionService;

    private Clock fixedClock;

    private WaitingTransferJob intentionToProceedJob;

    private final String cronExpression = "0 * * * * *";

    private static final LocalDate TODAY = LocalDate.of(2020, 3, 3);

    @Before
    public void setup() {
        fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        intentionToProceedJob = new WaitingTransferJob();
        intentionToProceedJob.setCronExpression(cronExpression);
        intentionToProceedJob.setScheduledStateTransitionService(scheduledStateTransitionService);
        intentionToProceedJob.setClock(fixedClock);
    }

    @Test
    public void executeShouldTriggerReadyForTransfer() throws Exception {
        intentionToProceedJob.execute(null);

        verify(scheduledStateTransitionService).stateChangeTriggered(
            eq(LocalDateTime.now(fixedClock)), eq(StateTransitions.WAITING_TRANSFER));
    }

    @Test(expected = JobExecutionException.class)
    public void shouldThrowJobExecutionException() throws Exception {
        doThrow(new RuntimeException()).when(scheduledStateTransitionService).stateChangeTriggered(any(), any());

        intentionToProceedJob.execute(null);
    }
}
