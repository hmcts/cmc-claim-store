package uk.gov.hmcts.cmc.scheduler.services;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public class NotificationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEmailService.class);

    @Retryable(
        value = {JobExecutionException.class},
        backoff = @Backoff(delay = 10000))
    public void process(JobExecutionContext context) throws JobExecutionException {
        logger.info("Now: " + ZonedDateTime.now());
        logger.info("Data: " + context.getJobDetail().getJobDataMap());
        logger.info("Completed job work...");
    }

    @Recover
    public void logSendMessageWithAttachmentFailure(JobExecutionException exception) {
        String errorMessage = String.format(
            "NotificationEmailJob failure:  failed to send email with due to %s", exception.getMessage()
        );
        logger.error(errorMessage, exception);
    }
}
