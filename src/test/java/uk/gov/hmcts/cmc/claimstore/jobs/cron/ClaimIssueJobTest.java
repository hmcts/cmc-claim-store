package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimIssueService;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClaimIssueJobTest {

    @InjectMocks
    ClaimIssueJob claimIssueJob;

    @Mock
    ClaimIssueService claimIssueService;

    @Mock
    LaunchDarklyClient launchDarklyClient;

    @Test
    public void shouldNotIssueCreatedClaimIfFeatureNotEnabled() throws JobExecutionException {
        claimIssueJob.execute(null);
        verify(claimIssueService, times(0)).issueCreatedClaims();
    }

    @Test
    public void shouldIssueCreatedClaimIfFeatureEnabled() throws JobExecutionException {
        when(launchDarklyClient.isFeatureEnabled("automated-claim-issue")).thenReturn(true);
        claimIssueJob.execute(null);
        verify(claimIssueService).issueCreatedClaims();
    }

}
