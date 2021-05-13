package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import lombok.Getter;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.CloseHWFClaimsInAwaitingStateService;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;

@Component
@Getter
@DisallowConcurrentExecution
public class CloseHWFClaimsInAwaitingStateJob implements CronJob {

    @Value("${schedule.close-hwf-claims-in-awaiting-state}")
    private String cronExpression;

    @Autowired
    private CloseHWFClaimsInAwaitingStateService closeHWFClaimsInAwaitingStateService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            closeHWFClaimsInAwaitingStateService.findCasesAndClose();
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

}
