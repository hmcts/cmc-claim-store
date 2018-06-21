package uk.gov.hmcts.cmc.claimstore.jobs;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ResponseNeededNotificationService;

@Component
public class NotificationEmailJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(NotificationEmailJob.class);

    @Autowired
    public ResponseNeededNotificationService responseNeededNotificationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        responseNeededNotificationService.sendMail(jobDetail);
        logger.info("Completed job work for id %s", jobDetail.getKey().getName());
    }
}
