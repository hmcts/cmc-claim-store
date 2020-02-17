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
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.CCJNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;

import java.time.LocalDate;
import java.util.List;

@Component
@Getter
@DisallowConcurrentExecution
public class NotifyClaimantDefaultCCJJob implements CronJob {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private CaseSearchApi caseSearchApi;

    private UserService userService;

    @Value("${dateCalculations.claimantCcjReminderDays:10}")
    private long claimantCCJReminderDays;

    private CCJNotificationService ccjNotificationService;

    @Value("${ccjClaimantNotify.schedule:0 0 8 ? * * *}")
    private String cronExpression;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            User anonymousCaseWorker = userService.authenticateAnonymousCaseWorker();
            List<Claim> claimsWithCCJ = caseSearchApi.getClaimsWithDefaultCCJ(anonymousCaseWorker,
                LocalDate.now().minusDays(claimantCCJReminderDays));
            claimsWithCCJ.forEach(claim -> ccjNotificationService.notifyClaimantAboutCCJReminder(claim));
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    @Autowired
    public void setCaseSearchApi(CaseSearchApi caseSearchApi) {
        this.caseSearchApi = caseSearchApi;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setCcjNotificationService(CCJNotificationService ccjNotificationService) {
        this.ccjNotificationService = ccjNotificationService;
    }
}
