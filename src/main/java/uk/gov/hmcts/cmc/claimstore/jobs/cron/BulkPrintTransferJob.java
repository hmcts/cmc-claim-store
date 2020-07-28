package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import lombok.Getter;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.BulkPrintTransferService;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;

@Component
@Getter
@DisallowConcurrentExecution
@ConditionalOnProperty("feature_toggles.ctsc_enabled")
public class BulkPrintTransferJob implements CronJob {

    @Value("${schedule.transfer-claims}")
    private String cronExpression;

    private  BulkPrintTransferService bulkPrintTransferService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            bulkPrintTransferService.findCasesAndTransfer();
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    @Autowired
    public void setBulkPrintTransferService(BulkPrintTransferService bulkPrintTransferService) {
        this.bulkPrintTransferService = bulkPrintTransferService;
    }
}
