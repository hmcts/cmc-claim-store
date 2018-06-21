package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.jobs.NotificationEmailJob;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.scheduler.model.JobData;
import uk.gov.hmcts.cmc.scheduler.services.JobService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class JobSchedulerService {

    private final JobService jobService;

    public JobSchedulerService(JobService jobService) {
        this.jobService = jobService;
    }

    public void scheduleEmailNotificationsForDefendantResponse(
        String defendantId,
        String defendantEmail,
        Claim claim
    ) {
        LocalDate responseDeadline = claim.getResponseDeadline();
        String defendantName = claim.getClaimData().getDefendant().getName();
        String claimantName = claim.getClaimData().getClaimant().getName();
        ImmutableMap.Builder<String, Object> emailData = ImmutableMap.builder();
        emailData.put("caseId", claim.getId());
        emailData.put("caseReference", claim.getReferenceNumber());
        emailData.put("defendantEmail", defendantEmail);
        emailData.put("defendantId", defendantId);
        emailData.put("defendantName", defendantName);
        emailData.put("claimantName", claimantName);
        emailData.put("responseDeadline", responseDeadline);

        jobService.scheduleJob(
            JobData.builder()
                .id("reminder:defence-due-in-5-days:" + claim.getReferenceNumber() + "-" + UUID.randomUUID().toString())
                .group("Reminders")
                .description("Defendant reminder email 5 days before response deadline")
                .jobClass(NotificationEmailJob.class)
                .data(emailData.build()).build(),
            responseDeadline.minusDays(5).atStartOfDay(ZoneOffset.UTC)
        );

        jobService.scheduleJob(
            JobData.builder()
                .id("reminder:defence-due-in-1-days:" + claim.getReferenceNumber() + "-" + UUID.randomUUID().toString())
                .group("Reminders")
                .description("Defendant reminder email 1 days before response deadline")
                .jobClass(NotificationEmailJob.class)
                .data(emailData.build()).build(),
            responseDeadline.minusDays(1).atStartOfDay(ZoneOffset.UTC));

    }
}
