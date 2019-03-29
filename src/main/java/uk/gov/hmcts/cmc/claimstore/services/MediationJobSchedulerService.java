package uk.gov.hmcts.cmc.claimstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.scheduler.model.JobData;
import uk.gov.hmcts.cmc.scheduler.services.JobService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Component
public class MediationJobSchedulerService {

    //should schedule email job

    private static final Logger logger = LoggerFactory.getLogger(JobSchedulerService.class);

    private final JobService jobService;

    @Autowired
    public MediationJobSchedulerService(JobService jobService) {
        this.jobService = jobService;
    }

    public void scheduleMediation() {

        ZonedDateTime scheduleMediationTime = LocalDate.now().atTime(5,0).atZone(ZoneOffset.UTC);
        jobService.scheduleJob(createMediationJobData(), scheduleMediationTime);
    }

    public JobData createMediationJobData() {
        return JobData.builder()
            .id("Mediation csv")
            .description("Mediation csv job for " + LocalDate.now().toString())
            .jobClass(MediationEmailJob.class)
            .build();
    }


}
