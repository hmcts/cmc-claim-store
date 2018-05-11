package uk.gov.hmcts.cmc.scheduler.services;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.Scheduler;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class JobServiceTest {

    @Mock
    private Scheduler scheduler;

    private JobService jobsService;

    @Before
    public void setUp() {
        jobsService = new JobService(scheduler);
    }

}
