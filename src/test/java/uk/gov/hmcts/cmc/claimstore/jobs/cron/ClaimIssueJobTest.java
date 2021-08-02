package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssueService;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ClaimIssueJobTest {

    @InjectMocks
    ClaimIssueJob claimIssueJob;

    @Mock
    ClaimIssueService claimIssueService;

    @Test
    public void shouldIssueCreatedClaim() throws JobExecutionException {
        claimIssueJob.execute(null);
        verify(claimIssueService).issueCreatedClaims();
    }

}
