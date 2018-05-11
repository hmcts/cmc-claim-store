package uk.gov.hmcts.cmc.scheduler.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class NotificationEmailJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEmailJob.class);

    @Override
    @Retryable(
        value = {JobExecutionException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 5000, maxDelay = 10000))
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //Note: This implementation is just for spike, the actual implementation needs to be implemented afterwards
        SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        logger.info("Executing NotificationEmailJob:: " + context.getJobDetail().getKey());
        logger.info("Now: " + ZonedDateTime.now());
        logger.info("Fire Time: " + formatter.format(context.getFireTime()));
        logger.info("Scheduled Fire Time: " + formatter.format(context.getScheduledFireTime()));
    }

    @Recover
    public void logSendMessageWithAttachmentFailure(JobExecutionException exception, JobExecutionContext context) {
        String errorMessage = String.format(
            "NotificationEmailJob failure:  failed to send email with details: %s due to %s",
            context.getJobDetail(), exception.getMessage()
        );
        logger.error(errorMessage, exception);
    }
}
