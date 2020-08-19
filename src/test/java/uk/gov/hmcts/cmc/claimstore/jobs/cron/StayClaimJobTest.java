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
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class StayClaimJobTest {

    @Mock
    private ScheduledStateTransitionService scheduledStateTransitionService;

    private Clock fixedClock;

    private StayClaimJob stayClaimJob;

    private final String cronExpression = "0 * * * * *";

    private static final LocalDate TODAY = LocalDate.of(2020, 3, 3);

    @Before
    public void setup() {
        fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        stayClaimJob = new StayClaimJob();
        stayClaimJob.setCronExpression(cronExpression);
        stayClaimJob.setScheduledStateTransitionService(scheduledStateTransitionService);
        stayClaimJob.setClock(fixedClock);
    }

    @Test
    public void executeShouldTriggerIntentionToProceed() throws Exception {
        stayClaimJob.execute(null);

        verify(scheduledStateTransitionService, once()).stateChangeTriggered(
            eq(LocalDateTime.now(fixedClock)), eq(StateTransitions.STAY_CLAIM));
    }

    @Test(expected = JobExecutionException.class)
    public void shouldThrowJobExecutionException() throws Exception {
        doThrow(new RuntimeException()).when(scheduledStateTransitionService).stateChangeTriggered(any(), any());

        stayClaimJob.execute(null);
    }
}
