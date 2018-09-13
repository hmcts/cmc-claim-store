package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.featuretoggle.FeatureTogglesApi;
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
    private final int firstReminderDay;
    private final int lastReminderDay;
    private final FeatureTogglesApi featureTogglesApi;

    public JobSchedulerService(
        JobService jobService,
        @Value("${dateCalculations.firstResponseReminderDay}") int firstReminderDay,
        @Value("${dateCalculations.lastResponseReminderDay}") int lastReminderDay,
        FeatureTogglesApi featureTogglesApi
    ) {
        this.jobService = jobService;
        this.firstReminderDay = firstReminderDay;
        this.lastReminderDay = lastReminderDay;
        this.featureTogglesApi = featureTogglesApi;
    }

    public void scheduleEmailNotificationsForDefendantResponse(Claim claim) {
        if (defenceRemindersAreEnabled()) {
            LocalDate responseDeadline = claim.getResponseDeadline();

            Map<String, Object> notificationData = ImmutableMap.of("caseReference", claim.getReferenceNumber());

            jobService.scheduleJob(
                createReminderJobData(claim, notificationData, firstReminderDay),
                responseDeadline.minusDays(firstReminderDay).atTime(8, 0).atZone(ZoneOffset.UTC)
            );

            jobService.scheduleJob(
                createReminderJobData(claim, notificationData, lastReminderDay),
                responseDeadline.minusDays(lastReminderDay).atTime(8, 0).atZone(ZoneOffset.UTC)
            );
        }
    }

    public void rescheduleEmailNotificationsForDefendantResponse(
        Claim claim,
        LocalDate responseDeadline
    ) {
        if (defenceRemindersAreEnabled()) {
            Map<String, Object> notificationData = ImmutableMap.of("caseReference", claim.getReferenceNumber());

            jobService.rescheduleJob(
                createReminderJobData(claim, notificationData, firstReminderDay),
                responseDeadline.minusDays(firstReminderDay).atTime(8, 0).atZone(ZoneOffset.UTC)
            );

            jobService.rescheduleJob(
                createReminderJobData(claim, notificationData, lastReminderDay),
                responseDeadline.minusDays(lastReminderDay).atTime(8, 0).atZone(ZoneOffset.UTC)
            );
        }
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

    private boolean defenceRemindersAreEnabled() {
        boolean defenceReminders;
        try {
            defenceReminders = featureTogglesApi.checkFeature("defenceReminders");
        } catch (FeignException exception) {
            defenceReminders = false;
        }
        return defenceReminders;
    }
}
