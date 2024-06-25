package uk.gov.hmcts.cmc.scheduler.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import uk.gov.hmcts.cmc.scheduler.config.CronJobConfiguration;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CronJobConfigurationTest {

    @Mock
    private JobService jobService;

    private CronJobConfiguration cronJobConfiguration;

    private final String cronExp = "cronExp";

    @BeforeEach
    public void setup() {
        CronJob cronJob = new CronJob() {
            @Override
            public String getCronExpression() {
                return cronExp;
            }

            @Override
            public void execute(JobExecutionContext context) {

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
