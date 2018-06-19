package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class NotificationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEmailService.class);
    private final ResponseNeededNotificationService responseNeededNotificationService;

    public NotificationEmailService(ResponseNeededNotificationService responseNeededNotificationService) {
        this.responseNeededNotificationService = responseNeededNotificationService;
    }

    @Retryable(
        value = {JobExecutionException.class},
        backoff = @Backoff(delay = 10000))
    public void process(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        responseNeededNotificationService.sendMail(jobDetail);
        logger.info("Completed job work for id %s", jobDetail.getKey().getName());
    }

    @Recover
    public void logSendMessageFailure(JobExecutionException exception) {
        String errorMessage = String.format(
            "NotificationEmailJob failure:  failed to send email with due to %s", exception.getMessage()
        );
        logger.error(errorMessage, exception);
    }
}
