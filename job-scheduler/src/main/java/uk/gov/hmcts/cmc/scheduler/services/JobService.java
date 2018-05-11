package uk.gov.hmcts.cmc.scheduler.services;

import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.scheduler.exceptions.JobException;
import uk.gov.hmcts.cmc.scheduler.model.JobData;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
public class JobService {

    private final Scheduler scheduler;

    @Autowired
    public JobService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public String scheduleJob(JobData jobData, ZonedDateTime startDateTime) {
        try {
            String id = UUID.randomUUID().toString();

            scheduler.scheduleJob(
                newJob(jobData.getJobClass())
                    .withIdentity(id, jobData.getGroup())
                    .withDescription(jobData.getDescription())
                    .usingJobData(new JobDataMap(jobData.getData()))
                    .requestRecovery()
                    .build(),
                newTrigger()
                    .startAt(Date.from(startDateTime.toInstant()))
                    .withSchedule(
                        simpleSchedule()
                            .withMisfireHandlingInstructionNowWithExistingCount()
                    )
                    .build()
            );

            return id;

        } catch (SchedulerException exc) {
            throw new JobException("Error while scheduling a job", exc);
        }
    }
}
