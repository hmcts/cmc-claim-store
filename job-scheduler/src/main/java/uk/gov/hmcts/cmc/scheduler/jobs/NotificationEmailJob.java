package uk.gov.hmcts.cmc.scheduler.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.scheduler.services.NotificationEmailService;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;

@Component
public class NotificationEmailJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEmailJob.class);

    @Autowired
    private NotificationEmailService notificationEmailService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //Note: This implementation is just for spike, the actual implementation needs to be implemented afterwards
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        logger.info("Executing NotificationEmailJob:: " + context.getJobDetail().getKey());
        logger.info("Now: " + ZonedDateTime.now());
        logger.info("Fire Time: " + formatter.format(context.getFireTime()));
        this.notificationEmailService.process(context);
    }
}
