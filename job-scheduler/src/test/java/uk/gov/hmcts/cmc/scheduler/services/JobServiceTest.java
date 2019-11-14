package uk.gov.hmcts.cmc.scheduler.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import uk.gov.hmcts.cmc.scheduler.model.JobData;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
        String group = "Reminders";
        JobData jobData = getJobData(jobId, group);

        ZonedDateTime startDateTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC);

        //when
        JobKey jobKey = jobsService.scheduleJob(jobData, startDateTime);
        //then

        assertThat(jobKey).isEqualTo(new JobKey(jobId, group));
        JobDetail jobDetail = getJobDetails(jobData);
        Trigger trigger = getTrigger(startDateTime, jobData);
        verify(scheduler).scheduleJob(jobDetail, trigger);
    }

    @Test
    public void shouldRescheduleOldJob() throws SchedulerException {
        //given
        String jobId = UUID.randomUUID().toString();
        String group = "Reminders";
        JobData jobData = getJobData(jobId, group);
        ZonedDateTime startDateTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC);

        //when

        //schedule new job
        JobKey jobKey = jobsService.scheduleJob(jobData, startDateTime);

        //reschedule job again
        ZonedDateTime newStartDateTime = LocalDate.now().plusDays(5).atStartOfDay(ZoneOffset.UTC);
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class)))
            .thenReturn(Date.from(newStartDateTime.toInstant()));

        jobsService.rescheduleJob(jobData, newStartDateTime);

        //then

        //verify scheduling
        assertThat(jobKey).isEqualTo(new JobKey(jobId, group));
        verify(scheduler).scheduleJob(getJobDetails(jobData), getTrigger(startDateTime, jobData));

        //verify rescheduling
        verify(scheduler).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
    }

    @Test
    public void shouldScheduleAnotherJobWhenReschedulingOldJobFails() throws SchedulerException {
        //given
        String jobId = UUID.randomUUID().toString();
        String group = "Reminders";
        JobData jobData = getJobData(jobId, group);

        ZonedDateTime startDateTime = LocalDate.now().atStartOfDay(ZoneOffset.UTC);

        //when
        //schedule new job
        JobKey jobKey = jobsService.scheduleJob(jobData, startDateTime);

        //reschedule job again
        ZonedDateTime newStartDateTime = LocalDate.now().plusDays(5).atStartOfDay(ZoneOffset.UTC);
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class)))
            .thenReturn(null);

        jobsService.rescheduleJob(jobData, newStartDateTime);

        //then
        //verify scheduling
        assertThat(jobKey).isEqualTo(new JobKey(jobId, group));
        verify(scheduler, atLeast(2)).scheduleJob(getJobDetails(jobData), getTrigger(startDateTime, jobData));
        //verify rescheduling
        verify(scheduler).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
    }

    @Test
    public void shouldScheduleNewCronJob() throws SchedulerException {
        String jobId = UUID.randomUUID().toString();
        String group = "Reminders";
        JobData jobData = getJobData(jobId, group);
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        String cronExpression = "0 * * * * ?";
        JobKey jobKey = jobsService.scheduleJob(jobData, cronExpression);

        assertThat(jobKey).isEqualTo(new JobKey(jobId, group));

        JobDetail jobDetail = getJobDetails(jobData);
        Trigger trigger = getCronTrigger(cronExpression, jobData);
        verify(scheduler).scheduleJob(jobDetail, trigger);
    }

    @Test
    public void shouldNotScheduleNewCronJobIfExpressionIsBlank() throws SchedulerException {
        String jobId = UUID.randomUUID().toString();
        String group = "Reminders";
        JobData jobData = getJobData(jobId, group);

        String cronExpression = "";
        JobKey jobKey = jobsService.scheduleJob(jobData, cronExpression);

        assertThat(jobKey).isNull();
        verify(scheduler, never()).scheduleJob(any(), any());
    }

    @Test
    public void shouldDeleteExistingCronJob() throws SchedulerException {
        String jobId = UUID.randomUUID().toString();
        String group = "Reminders";
        JobData jobData = getJobData(jobId, group);
        String cronExpression = "0 * * * * ?";
        JobKey key = getJobDetails(jobData).getKey();
        when(scheduler.checkExists(key)).thenReturn(true);

        JobKey jobKey = jobsService.scheduleJob(jobData, cronExpression);

        verify(scheduler).deleteJob(key);
    }

    private JobData getJobData(String jobId, String group) {
        Map<String, Object> data = new HashMap<>();
        data.put("caseId", "234324332432432");
        data.put("caseReference", "000MC003");
        data.put("defendantEmail", "j.smith@example.com");

        return JobData.builder()
            .id(jobId)
            .group(group)
            .description("Mock job scheduler")
            .data(data)
            .jobClass(Job.class)
            .build();
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

    private Trigger getCronTrigger(String cronExpression, JobData jobData) {
        return newTrigger()
            .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
            .withIdentity(jobData.getId(), jobData.getGroup())
            .withDescription(jobData.getDescription())
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
