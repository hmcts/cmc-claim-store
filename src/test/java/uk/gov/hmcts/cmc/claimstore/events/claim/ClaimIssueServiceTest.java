package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.events.ClaimCreationEvent;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClaimIssueServiceTest {

    @InjectMocks
    private ClaimIssueService claimIssueService;

    @Mock
    private UserService userService;

    @Mock
    private ClaimService claimService;

    @Mock
    private PostClaimOrchestrationHandler postClaimOrchestrator;

    @Captor
    ArgumentCaptor<CitizenClaimCreatedEvent> citizenClaimCreatedEventCaptor;

    @Captor
    ArgumentCaptor<RepresentedClaimCreatedEvent> representedClaimCreatedEventCaptor;

    private static final Claim citizenClaim = SampleClaim.getCitizenClaim();
    private static final Claim representedClaim = SampleClaim.getLegalDataWithReps();

    @Before
    public void setUp() {
        User user = new User("123", null);
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(user);
        when(claimService.getClaimsByState(ClaimState.CREATE, user)).thenReturn(asList(citizenClaim, representedClaim));
        doNothing().when(postClaimOrchestrator).citizenIssueHandler(any(CitizenClaimCreatedEvent.class));
        doNothing().when(postClaimOrchestrator).representativeIssueHandler(any(RepresentedClaimCreatedEvent.class));
    }

    @Test
    public void shouldIssueCreatedClaims() {
        claimIssueService.issueCreatedClaims();
        verify(postClaimOrchestrator).citizenIssueHandler(citizenClaimCreatedEventCaptor.capture());
        verify(postClaimOrchestrator).representativeIssueHandler(representedClaimCreatedEventCaptor.capture());
        verifyEvent(citizenClaimCreatedEventCaptor.getValue(), "John Rambo", "123", citizenClaim);
        verifyEvent(representedClaimCreatedEventCaptor.getValue(), "Trading ltd", "123", representedClaim);
    }

    private void verifyEvent(ClaimCreationEvent claimCreationEvent, String name, String authorization, Claim claim) {
        assertEquals(name, claimCreationEvent.getSubmitterName());
        assertEquals(authorization, claimCreationEvent.getAuthorisation());
        assertEquals(claim, claimCreationEvent.getClaim());
    }

}
