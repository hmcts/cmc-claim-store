package uk.gov.hmcts.cmc.scheduler.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;
import uk.gov.hmcts.cmc.scheduler.model.JobData;
import uk.gov.hmcts.cmc.scheduler.services.JobService;

import java.util.HashMap;
import javax.annotation.PostConstruct;

@Configuration
public class CronJobConfiguration {

    private final JobService jobService;
    private final CronJob[] cronJobs;

    @Autowired
    public CronJobConfiguration(JobService jobService, CronJob... cronJobs) {
        this.jobService = jobService;
        this.cronJobs = cronJobs;
    }

    @PostConstruct
    public void init() {
        for (CronJob cronJob : cronJobs) {

            JobData jobData = JobData.builder()
                .id(cronJob.getClass().getName())
                .jobClass(cronJob.getClass())
                .data(new HashMap<>())
                .build();

            jobService.scheduleJob(jobData, cronJob.getCronExpression());
        }
    }
}
