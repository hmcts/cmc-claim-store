package uk.gov.hmcts.cmc.scheduler.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.cmc.scheduler.config.CronJobConfiguration;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CronJobConfigurationTest {

    @Mock
    private JobService jobService;

    private CronJobConfiguration cronJobConfiguration;

    private final String cronExp = "cronExp";

    @Before
    public void setup() {
        CronJob cronJob = new CronJob() {
            @Override
            public String getCronExpression() {
                return cronExp;
            }

            @Override
            public void execute(JobExecutionContext context) throws JobExecutionException {

            }
        };

        cronJobConfiguration = new CronJobConfiguration(jobService, cronJob, cronJob);
    }

    @Test
    public void shouldScheduleCronJobs() {
        cronJobConfiguration.init();

        verify(jobService, times(2)).scheduleJob(any(), eq(cronExp));
    }
}
