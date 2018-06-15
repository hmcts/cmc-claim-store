package uk.gov.hmcts.cmc.claimstore.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationEmailService;

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
