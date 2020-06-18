package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import lombok.Getter;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.BulkPrintTransferService;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;

@Component
@Getter
@DisallowConcurrentExecution
public class BulkPrintTransferJob implements CronJob {

    private String cronExpression;

    private BulkPrintTransferService bulkPrintTransferService;

    @Autowired
    public BulkPrintTransferJob(
        @Value("${claimsReadyForTransfer.schedule:0 0 8 ? * * *}") String cronExpression,
        BulkPrintTransferService bulkPrintTransferService
    ) {
        this.cronExpression = cronExpression;
        this.bulkPrintTransferService = bulkPrintTransferService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            bulkPrintTransferService.bulkPrintTransfer();

        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
