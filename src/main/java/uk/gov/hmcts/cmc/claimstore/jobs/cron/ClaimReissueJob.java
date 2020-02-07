package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import lombok.Getter;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.ClaimReissueService;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;

@Component
@Getter
@DisallowConcurrentExecution
public class ClaimReissueJob implements CronJob {

    private ClaimReissueService claimReissueService;
    @Value("${claim_reissue.schedule}")
    private String cronExpression;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
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
