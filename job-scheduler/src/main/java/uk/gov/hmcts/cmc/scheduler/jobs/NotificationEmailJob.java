package uk.gov.hmcts.cmc.scheduler.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.scheduler.services.NotificationEmailService;

@Component
public class NotificationEmailJob implements Job {

    private NotificationEmailService notificationEmailService;

    public NotificationEmailJob(
        NotificationEmailService notificationEmailService) {
        this.notificationEmailService = notificationEmailService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.notificationEmailService.process(context);
    }
}
