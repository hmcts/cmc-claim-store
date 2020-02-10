package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.PostClaimOrchestrationHandler;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.CREATE;

@RunWith(MockitoJUnitRunner.class)
public class ClaimReissueServiceTest {

    private ClaimReissueService claimReissueService;

    @Mock
    private UserService userService;

    @Mock
    private ClaimService claimService;

    @Mock
    PostClaimOrchestrationHandler postClaimOrchestrationHandler;
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final UserDetails USER_DETAILS = SampleUserDetails.builder().build();
    private static final User USER = new User(AUTHORISATION, USER_DETAILS);

    @Before
    public void setUp() {
        claimReissueService = new ClaimReissueService(
            claimService,
            userService,
            postClaimOrchestrationHandler
        );
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(USER);
    }

    @Test
    public void shouldCreateAndReissueCitizenClaim() {

        when(claimService.getClaimsByState(eq(CREATE), any()))
            .thenReturn(singletonList(SampleClaim.getDefault()));
        claimReissueService.getCreatedClaimsAndReIssue();
        verify(postClaimOrchestrationHandler)
            .citizenIssueHandler(any(CitizenClaimCreatedEvent.class));
    }

    @Test
    public void shouldCreateAndReissueRepresentativeClaim() {

        when(claimService.getClaimsByState(eq(CREATE), any()))
            .thenReturn(singletonList(SampleClaim.getDefaultForLegal()));
        claimReissueService.getCreatedClaimsAndReIssue();
        verify(postClaimOrchestrationHandler)
            .representativeIssueHandler(any(RepresentedClaimCreatedEvent.class));
    }

    @Test(expected = Exception.class)
    public void shouldThrowExceptionWhenSomethingGoesWrong() {
        when(claimService.getClaimsByState(eq(CREATE), any())).thenThrow(Exception.class);
        claimReissueService.getCreatedClaimsAndReIssue();
    }
}
