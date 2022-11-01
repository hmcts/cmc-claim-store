package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import lombok.Getter;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.TransferCaseStayedService;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;


@Component
@Getter
@DisallowConcurrentExecution
public class ScheduleCaseStayedStateTransition implements CronJob {

    @Value("${schedule.transfer-stayed-claims}")
    private String cronExpression;

    private TransferCaseStayedService caseStayedTransferService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            caseStayedTransferService.findCasesForTransfer();
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    @Autowired
    public void setCaseStayedTransferService(TransferCaseStayedService caseStayedTransferService) {
        this.caseStayedTransferService = caseStayedTransferService;
    }
}
