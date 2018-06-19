package uk.gov.hmcts.cmc.scheduler.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import uk.gov.hmcts.cmc.scheduler.model.JobData;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@RunWith(MockitoJUnitRunner.class)
public class JobServiceTest {

    @Mock
    private Scheduler scheduler;

    private JobService jobsService;

    @Before
    public void setUp() {
        jobsService = new JobService(scheduler);
    }

    @Test
    public void shouldScheduleNewJob() throws SchedulerException {
        //given
        String jobId = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("caseId", "234324332432432");
        data.put("caseReference", "000MC003");
        data.put("defendantEmail", "j.smith@example.com");
        String group = "Reminders";

        JobData jobData = JobData.builder()
            .id(jobId)
            .group(group)
            .description("Mock job scheduler")
            .data(data)
            .jobClass(Job.class)
            .build();

        ZonedDateTime startDateTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC);

        //when
        JobKey jobKey = jobsService.scheduleJob(jobData, startDateTime);
        //then

        assertThat(jobKey).isEqualTo(new JobKey(jobId, group));
        JobDetail jobDetail = getJobDetails(jobData);
        Trigger trigger = getTrigger(startDateTime, jobData);
        verify(scheduler).scheduleJob(jobDetail, trigger);
    }

    private Trigger getTrigger(ZonedDateTime startDateTime, JobData jobData) {
        return newTrigger()
            .startAt(Date.from(startDateTime.toInstant()))
            .withIdentity(jobData.getId(), jobData.getGroup())
            .withSchedule(
                simpleSchedule()
                    .withMisfireHandlingInstructionNowWithExistingCount()
            )
            .build();
    }

    private JobDetail getJobDetails(JobData jobData) {
        return JobBuilder.newJob(jobData.getJobClass())
            .withIdentity(jobData.getId(), jobData.getGroup())
            .withDescription(jobData.getDescription())
            .usingJobData(new JobDataMap(jobData.getData()))
            .requestRecovery()
            .build();
    }

}
