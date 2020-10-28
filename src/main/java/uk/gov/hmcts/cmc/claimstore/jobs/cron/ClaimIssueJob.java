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
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssueService;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;

@Component
@Getter
@DisallowConcurrentExecution
public class ClaimIssueJob implements CronJob {

    private final Logger logger = LoggerFactory.getLogger(ClaimIssueJob.class);

    @Autowired
    private ClaimIssueService claimIssueService;

    @Value("${schedule.issue-created-claims}")
    private String cronExpression;

    @Autowired
    private LaunchDarklyClient launchDarklyClient;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            if (launchDarklyClient.isFeatureEnabled("automated-claim-issue")) {
                claimIssueService.issueCreatedClaims();
            }
        } catch (Exception e) {
            logger.error("Automated Claim Issue - Failed: ", e);
        }
    }

}
