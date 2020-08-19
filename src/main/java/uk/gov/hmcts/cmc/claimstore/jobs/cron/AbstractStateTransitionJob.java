package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import lombok.Getter;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.services.ScheduledStateTransitionService;
import uk.gov.hmcts.cmc.claimstore.services.statetransition.StateTransition;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;

import java.time.Clock;
import java.time.LocalDateTime;

@Getter
@DisallowConcurrentExecution
public abstract class AbstractStateTransitionJob implements CronJob {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private ScheduledStateTransitionService scheduledStateTransitionService;

    private Clock clock;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            logger.info("State transition executed for {}", getStateTransition());
            scheduledStateTransitionService.stateChangeTriggered(LocalDateTime.now(clock), getStateTransition());
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    @Autowired
    public void setScheduledStateTransitionService(ScheduledStateTransitionService scheduledStateTransitionService) {
        this.scheduledStateTransitionService = scheduledStateTransitionService;
    }

    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    protected abstract StateTransition getStateTransition();

    protected abstract void setCronExpression(String cronExpression);
}
