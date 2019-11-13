package uk.gov.hmcts.cmc.scheduler.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.ListableBeanFactory;
import uk.gov.hmcts.cmc.scheduler.config.CronJobConfiguration;
import uk.gov.hmcts.cmc.scheduler.model.CronJob;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CronJobConfigurationTest {

    @Mock
    private ListableBeanFactory beanFactory;

    @Mock
    private JobService jobService;

    private CronJobConfiguration cronJobConfiguration;

    @Before
    public void setup(){
        cronJobConfiguration = new CronJobConfiguration(jobService);
        cronJobConfiguration.setBeanFactory(beanFactory);
    }

    @Test
    public void shouldScheduleCronJobs(){
        String cronExp = "cronExp";
        CronJob cronJob = new CronJob() {
            @Override
            public String getCronExpression() {
                return cronExp;
            }

            @Override
            public void execute(JobExecutionContext context) throws JobExecutionException {

            }
        };
        when(beanFactory.getBeansOfType(CronJob.class)).thenReturn(ImmutableMap.of("bean", cronJob));

        cronJobConfiguration.init();

        verify(jobService).scheduleJob(any(), eq(cronExp));
    }
}
