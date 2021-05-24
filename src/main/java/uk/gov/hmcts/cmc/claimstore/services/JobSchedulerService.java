package uk.gov.hmcts.cmc.claimstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.jobs.NotificationEmailJob;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.scheduler.model.JobData;
import uk.gov.hmcts.cmc.scheduler.services.JobService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

@Service
public class JobSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(JobSchedulerService.class);

    private final JobService jobService;
    private final int firstReminderDay;
    private final int lastReminderDay;
    private final boolean enabled;

    public JobSchedulerService(
        JobService jobService,
        @Value("${dateCalculations.firstResponseReminderDay}") int firstReminderDay,
        @Value("${dateCalculations.lastResponseReminderDay}") int lastReminderDay,
        @Value("${feature_toggles.reminderEmails}") boolean enabled
    ) {
        this.jobService = jobService;
        this.firstReminderDay = firstReminderDay;
        this.lastReminderDay = lastReminderDay;
        this.enabled = enabled;
    }

    public void scheduleEmailNotificationsForDefendantResponse(Claim claim) {
        LocalDate responseDeadline = claim.getResponseDeadline();
        ZonedDateTime firstReminderDate = calculateReminderDate(responseDeadline, firstReminderDay);
        ZonedDateTime lastReminderDate = calculateReminderDate(responseDeadline, lastReminderDay);

        if (!enabled) {
            logger.debug("Reminder emails disabled. Skipping reminders for claim {} at {} and {}",
                claim.getReferenceNumber(), firstReminderDate, lastReminderDate);
            return;
        }

        Map<String, Object> notificationData = Map.of("caseReference", claim.getReferenceNumber());

        jobService.scheduleJob(
            createReminderJobData(claim, notificationData, firstReminderDay),
            firstReminderDate
        );

        jobService.scheduleJob(
            createReminderJobData(claim, notificationData, lastReminderDay),
            lastReminderDate
        );

    }

    public void rescheduleEmailNotificationsForDefendantResponse(
        Claim claim,
        LocalDate responseDeadline
    ) {
        ZonedDateTime firstReminderDate = calculateReminderDate(responseDeadline, firstReminderDay);
        ZonedDateTime lastReminderDate = calculateReminderDate(responseDeadline, lastReminderDay);

        if (!enabled) {
            logger.debug("Reminder emails disabled. Skipping rescheduled reminders for claim {} at {} and {}",
                claim.getReferenceNumber(), firstReminderDate, lastReminderDate);
            return;
        }

        Map<String, Object> notificationData = Map.of("caseReference", claim.getReferenceNumber());

        jobService.rescheduleJob(
            createReminderJobData(claim, notificationData, firstReminderDay),
            firstReminderDate
        );

        jobService.rescheduleJob(
            createReminderJobData(claim, notificationData, lastReminderDay),
            lastReminderDate
        );

    }

    private JobData createReminderJobData(Claim claim, Map<String, Object> data, int numberOfDaysBeforeDeadline) {
        String jobId = String.format("reminder:defence-due-in-%d-day%s:%s-%s",
            numberOfDaysBeforeDeadline,
            numberOfDaysBeforeDeadline > 1 ? "s" : "",
            claim.getReferenceNumber(),
            claim.getExternalId()
        );

        String description = String.format("Defendant reminder email %d day%s before response deadline",
            numberOfDaysBeforeDeadline,
            numberOfDaysBeforeDeadline > 1 ? "s" : ""
        );

        return JobData.builder()
            .id(jobId)
            .group("Reminders")
            .description(description)
            .jobClass(NotificationEmailJob.class)
            .data(data).build();
    }

    private static ZonedDateTime calculateReminderDate(LocalDate responseDeadline, int reminderDays) {
        return responseDeadline.minusDays(reminderDays).atTime(8, 0).atZone(ZoneOffset.UTC);
    }

}
