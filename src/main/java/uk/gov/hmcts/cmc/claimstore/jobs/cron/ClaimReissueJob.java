package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import lombok.NoArgsConstructor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.ClaimReissueService;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;

@DisallowConcurrentExecution
@NoArgsConstructor
@Component
public class ClaimReissueJob implements CronJob {

    private ClaimReissueService claimReissueService;

    private String cronExpression;

    @Override
    public String getCronExpression() {
        return cronExpression;
    }

    @Autowired
    public void setCronExpression(@Value("0 0/2 0 ? * * *") String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            System.out.println("hi akriti I am executing");
            claimReissueService.getCreatedClaimsAndReIssue();
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    @Autowired
    public void setClaimReissueService(ClaimReissueService claimReissueService) {
        this.claimReissueService = claimReissueService;
    }
}
