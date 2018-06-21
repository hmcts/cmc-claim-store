package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.jobs.NotificationEmailJob;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.scheduler.model.JobData;
import uk.gov.hmcts.cmc.scheduler.services.JobService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
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

        Map<String, Object> data = ImmutableMap.<String, Object>builder()
            .put("caseId", claim.getId())
            .put("caseReference", claim.getReferenceNumber())
            .put("defendantEmail", defendantEmail)
            .put("defendantId", defendantId)
            .put("defendantName", defendantName)
            .put("claimantName", claimantName)
            .put("responseDeadline", responseDeadline)
            .build();

        jobService.scheduleJob(
            JobData.builder()
                .id("reminder:defence-due-in-5-days:" + claim.getReferenceNumber() + "-" + UUID.randomUUID().toString())
                .group("Reminders")
                .description("Defendant reminder email 5 days before response deadline")
                .jobClass(NotificationEmailJob.class)
                .data(data).build(),
            responseDeadline.minusDays(5).atStartOfDay(ZoneOffset.UTC)
        );

        jobService.scheduleJob(
            JobData.builder()
                .id("reminder:defence-due-in-1-days:" + claim.getReferenceNumber() + "-" + UUID.randomUUID().toString())
                .group("Reminders")
                .description("Defendant reminder email 1 days before response deadline")
                .jobClass(NotificationEmailJob.class)
                .data(data).build(),
            responseDeadline.minusDays(1).atStartOfDay(ZoneOffset.UTC));

    }
}
