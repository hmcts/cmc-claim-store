package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.cmc.claimstore.services.ClaimReissueService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ClaimReissueJobTest {

    @Mock
    private ClaimReissueService claimReissueService;

    private ClaimReissueJob claimReissueJob;

    @Before
    public void setup() {
        claimReissueJob = new ClaimReissueJob();
        claimReissueService = mock(ClaimReissueService.class);
        claimReissueJob.setClaimReissueService(claimReissueService);
    }

    @Test
    public void executeShouldTriggerClaimReIssue() throws Exception {

        claimReissueJob.execute(null);
        verify(claimReissueService).getCreatedClaimsAndReIssue();
    }

    @Test(expected = JobExecutionException.class)
    public void shouldThrowJobExecutionException() throws Exception {
        doThrow(new RuntimeException()).when(claimReissueService).getCreatedClaimsAndReIssue();

        claimReissueJob.execute(null);
    }
}
