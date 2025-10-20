package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.quartz.JobExecutionException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.cmc.claimstore.services.ScheduledStateTransitionService;
import uk.gov.hmcts.cmc.claimstore.services.statetransition.StateTransitions;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class WaitingTransferJobTest {

    @Mock
    private ScheduledStateTransitionService scheduledStateTransitionService;

    private Clock fixedClock;

    private WaitingTransferJob intentionToProceedJob;

    private final String cronExpression = "0 * * * * *";

    private static final LocalDate TODAY = LocalDate.of(2020, 3, 3);

    @BeforeEach
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

    @Test
    public void shouldThrowJobExecutionException() throws Exception {
        doThrow(new RuntimeException()).when(scheduledStateTransitionService).stateChangeTriggered(any(), any());

        assertThrows(JobExecutionException.class, () -> {
            intentionToProceedJob.execute(null);
        });
    }
}
