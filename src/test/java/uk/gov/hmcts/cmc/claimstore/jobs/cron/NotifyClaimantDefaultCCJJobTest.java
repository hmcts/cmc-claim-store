package uk.gov.hmcts.cmc.claimstore.jobs.cron;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseSearchApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.CCJNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotifyClaimantDefaultCCJJobTest {

    @Mock
    private CaseSearchApi caseSearchApi;

    @Mock
    private UserService userService;

    @Mock
    private CCJNotificationService ccjNotificationService;

    private NotifyClaimantDefaultCCJJob notifyClaimantDefaultCCJJob;

    private static final String AUTHORISATION = "Auth";

    private static final User USER = new User(AUTHORISATION, SampleUserDetails.builder().build());

    @Before
    public void setup() {
        notifyClaimantDefaultCCJJob = new NotifyClaimantDefaultCCJJob();
        notifyClaimantDefaultCCJJob.setCaseSearchApi(caseSearchApi);
        notifyClaimantDefaultCCJJob.setUserService(userService);
        notifyClaimantDefaultCCJJob.setCcjNotificationService(ccjNotificationService);

        when(userService.authenticateAnonymousCaseWorker()).thenReturn(USER);
    }

    @Test
    public void executeShouldNotifyClaimant() throws Exception {
        Claim singleClaim = SampleClaim.getCitizenClaim();
        when(caseSearchApi.getClaimsWithDefaultCCJ(eq(USER), ArgumentMatchers.any()))
            .thenReturn(Collections.singletonList(singleClaim));
        notifyClaimantDefaultCCJJob.execute(null);

        verify(ccjNotificationService, times(1)).notifyClaimantAboutCCJReminder(singleClaim);
    }
}
