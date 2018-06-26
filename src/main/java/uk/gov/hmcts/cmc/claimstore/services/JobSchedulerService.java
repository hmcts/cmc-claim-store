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

        Map<String, Object> data = getNotificationData(authorisation, claim, responseDeadline);

        jobService.scheduleJob(
            getFirstReminderJobData(claim, data),
            responseDeadline.minusDays(firstReminderDay).atStartOfDay(ZoneOffset.UTC)
        );

        jobService.scheduleJob(
            getLastReminderJobData(claim, data),
            responseDeadline.minusDays(lastReminderDay).atStartOfDay(ZoneOffset.UTC)
        );

    }

    public void rescheduleEmailNotificationsForDefendantResponse(
        String authorisation,
        Claim claim,
        LocalDate responseDeadline
    ) {
        Map<String, Object> data = getNotificationData(authorisation, claim, responseDeadline);

        jobService.rescheduleJob(
            getFirstReminderJobData(claim, data),
            responseDeadline.minusDays(firstReminderDay).atStartOfDay(ZoneOffset.UTC)
        );

        jobService.rescheduleJob(
            getLastReminderJobData(claim, data),
            responseDeadline.minusDays(lastReminderDay).atStartOfDay(ZoneOffset.UTC)
        );

    }

    private JobData getLastReminderJobData(Claim claim, Map<String, Object> data) {
        return JobData.builder()
            .id("reminder:defence-due-in-"
                + lastReminderDay
                + "-day:"
                + claim.getReferenceNumber()
                + "-"
                + claim.getExternalId()
            )
            .group("Reminders")
            .description("Defendant reminder email " + lastReminderDay + " day before response deadline")
            .jobClass(NotificationEmailJob.class)
            .data(data).build();
    }

    private JobData getFirstReminderJobData(Claim claim, Map<String, Object> data) {
        return JobData.builder()
            .id("reminder:defence-due-in-"
                + firstReminderDay
                + "-days:"
                + claim.getReferenceNumber()
                + "-"
                + claim.getExternalId()
            )
            .group("Reminders")
            .description("Defendant reminder email " + firstReminderDay + " days before response deadline")
            .jobClass(NotificationEmailJob.class)
            .data(data).build();
    }

    private Map<String, Object> getNotificationData(String authorisation, Claim claim, LocalDate responseDeadline) {
        User defendantUser = userService.getUser(authorisation);

        String defendantId = defendantUser.getUserDetails().getId();
        String defendantEmail = defendantUser.getUserDetails().getEmail();
        String defendantName = claim.getClaimData().getDefendant().getName();
        String claimantName = claim.getClaimData().getClaimant().getName();

        return ImmutableMap.<String, Object>builder()
            .put("caseId", claim.getId())
            .put("caseReference", claim.getReferenceNumber())
            .put("defendantEmail", defendantEmail)
            .put("defendantId", defendantId)
            .put("defendantName", defendantName)
            .put("claimantName", claimantName)
            .put("responseDeadline", responseDeadline)
            .build();
    }
}
