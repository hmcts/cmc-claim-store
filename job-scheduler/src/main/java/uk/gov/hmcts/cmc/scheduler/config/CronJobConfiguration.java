package uk.gov.hmcts.cmc.scheduler.config;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;
import uk.gov.hmcts.cmc.scheduler.model.JobData;
import uk.gov.hmcts.cmc.scheduler.services.JobService;

import java.util.HashMap;
import javax.annotation.PostConstruct;

@Configuration
public class CronJobConfiguration {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JobService jobService;
    private final CronJob[] cronJobs;

    @Autowired
    public CronJobConfiguration(JobService jobService, CronJob... cronJobs) {
        this.jobService = jobService;
        this.cronJobs = cronJobs;
    }

    @PostConstruct
    public void init() {
        try {
            jobService.clearJobs();
        } catch (SchedulerException e) {
            logger.error("Error clearing jobs", e);
        }

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
