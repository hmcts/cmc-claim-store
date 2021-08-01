package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssueService;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
