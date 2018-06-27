package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.jobs.NotificationEmailJob;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.scheduler.model.JobData;
import uk.gov.hmcts.cmc.scheduler.services.JobService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;

@Service
public class JobSchedulerService {

    private final JobService jobService;
    private final UserService userService;
    private final int firstReminderDay;
    private final int lastReminderDay;

    public JobSchedulerService(
        JobService jobService,
        UserService userService,
        @Value("${dateCalculations.firstResponseReminderDay}") int firstReminderDay,
        @Value("${dateCalculations.lastResponseReminderDay}") int lastReminderDay
    ) {
        this.jobService = jobService;
        this.userService = userService;
        this.firstReminderDay = firstReminderDay;
        this.lastReminderDay = lastReminderDay;
    }

    public void scheduleEmailNotificationsForDefendantResponse(
        String authorisation,
        Claim claim
    ) {
        LocalDate responseDeadline = claim.getResponseDeadline();

        Map<String, Object> data = createNotificationData(authorisation, claim.getReferenceNumber());

        jobService.scheduleJob(
            createReminderJobData(claim, data, firstReminderDay),
            responseDeadline.minusDays(firstReminderDay).atStartOfDay(ZoneOffset.UTC)
        );

        jobService.scheduleJob(
            createReminderJobData(claim, data, lastReminderDay),
            responseDeadline.minusDays(lastReminderDay).atStartOfDay(ZoneOffset.UTC)
        );

    }

    public void rescheduleEmailNotificationsForDefendantResponse(
        String authorisation,
        Claim claim,
        LocalDate responseDeadline
    ) {
        Map<String, Object> data = createNotificationData(authorisation, claim.getReferenceNumber());

        jobService.rescheduleJob(
            createReminderJobData(claim, data, firstReminderDay),
            responseDeadline.minusDays(firstReminderDay).atStartOfDay(ZoneOffset.UTC)
        );

        jobService.rescheduleJob(
            createReminderJobData(claim, data, lastReminderDay),
            responseDeadline.minusDays(lastReminderDay).atStartOfDay(ZoneOffset.UTC)
        );

    }

    private JobData createReminderJobData(Claim claim, Map<String, Object> data, int numberOfDaysBeforeDeadline) {
        return JobData.builder()
            .id("reminder:defence-due-in-"
                + numberOfDaysBeforeDeadline
                + "-day"
                + (numberOfDaysBeforeDeadline > 1 ? "s" : "")
                + ":"
                + claim.getReferenceNumber()
                + "-"
                + claim.getExternalId()
            )
            .group("Reminders")
            .description("Defendant reminder email "
                + numberOfDaysBeforeDeadline
                + " day"
                + (numberOfDaysBeforeDeadline > 1 ? "s" : "")
                + " before response deadline"
            )
            .jobClass(NotificationEmailJob.class)
            .data(data).build();
    }

    private Map<String, Object> createNotificationData(String authorisation, String referenceNumber) {
        User defendantUser = userService.getUser(authorisation);
        String defendantEmail = defendantUser.getUserDetails().getEmail();

        return ImmutableMap.<String, Object>builder()
            .put("caseReference", referenceNumber)
            .put("defendantEmail", defendantEmail)
            .build();
    }
}
