package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.scheduler.jobs.NotificationEmailJob;
import uk.gov.hmcts.cmc.scheduler.model.JobData;
import uk.gov.hmcts.cmc.scheduler.services.JobService;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Component
public class ClaimIssuedCitizenActionsHandler {

    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final NotificationsProperties notificationsProperties;
    private final JobService jobService;

    @Autowired
    public ClaimIssuedCitizenActionsHandler(
        ClaimIssuedNotificationService claimIssuedNotificationService,
        NotificationsProperties notificationsProperties,
        JobService jobService
    ) {
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
        this.jobService = jobService;
    }

    @EventListener
    public void sendClaimantNotification(CitizenClaimIssuedEvent event) {
        Claim claim = event.getClaim();

        claimIssuedNotificationService.sendMail(
            claim,
            claim.getSubmitterEmail(),
            null,
            getEmailTemplates().getClaimantClaimIssued(),
            "claimant-issue-notification-" + claim.getReferenceNumber(),
            event.getSubmitterName()
        );
    }

    @EventListener
    public void sendDefendantNotification(CitizenClaimIssuedEvent event) {
        Claim claim = event.getClaim();

        if (!claim.getClaimData().isClaimantRepresented()) {
            claim.getClaimData().getDefendant().getEmail()
                .ifPresent(defendantEmail ->
                    claimIssuedNotificationService.sendMail(
                        claim,
                        defendantEmail,
                        event.getPin(),
                        getEmailTemplates().getDefendantClaimIssued(),
                        "defendant-issue-notification-" + claim.getReferenceNumber(),
                        event.getSubmitterName()
                    ));
        }
    }

    // This is added for spike, the actual implementation may vary based on the requirements in story
    @EventListener
    public void scheduleReminderEmails(CitizenClaimIssuedEvent event) {
        Claim claim = event.getClaim();
        Map<String, Object> data = new HashMap<>();
        data.put("Email", claim.getDefendantEmail()); // we can add whatever data we want to pass to job.

        JobData fiveDaysReminder = JobData.builder()
            .startDateTime(claim.getResponseDeadline().minusDays(5).atStartOfDay(ZoneOffset.UTC))
            .group("Reminders")
            .description("Defendant reminder email 5 days before response deadline")
            .jobClass(NotificationEmailJob.class)
            .data(data)
            .build();

        this.jobService.scheduleJob(fiveDaysReminder);

        JobData oneDayReminder = JobData.builder()
            .startDateTime(claim.getResponseDeadline().minusDays(1).atStartOfDay(ZoneOffset.UTC))
            .group("Reminders")
            .description("Defendant reminder email 1 days before response deadline")
            .jobClass(NotificationEmailJob.class)
            .data(data)
            .build();

        this.jobService.scheduleJob(oneDayReminder);

    }

    private EmailTemplates getEmailTemplates() {
        NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
