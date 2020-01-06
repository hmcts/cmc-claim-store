package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import lombok.Getter;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.MediationReportService;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;

@Component
@Getter
@DisallowConcurrentExecution
public class MiloJob implements CronJob {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private MediationReportService mediationReportService;

    @Value("${milo.schedule}")
    private String cronExpression;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            mediationReportService.automatedMediationReport();
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    @Autowired
    public void setMediationReportService(MediationReportService mediationReportService) {
        this.mediationReportService = mediationReportService;
    }
}
