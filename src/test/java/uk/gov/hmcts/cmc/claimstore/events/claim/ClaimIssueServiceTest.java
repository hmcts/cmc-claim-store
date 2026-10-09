package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.events.ClaimCreationEvent;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClaimIssueServiceTest {

    @InjectMocks
    private ClaimIssueService claimIssueService;

    @Mock
    private UserService userService;

    @Mock
    private CCDCaseApi ccdCaseApi;

    @Mock
    private CaseMapper caseMapper;
    @Mock
    private PostClaimOrchestrationHandler postClaimOrchestrator;

    @Captor
    ArgumentCaptor<CitizenClaimCreatedEvent> citizenClaimCreatedEventCaptor;

    private static final Claim citizenClaim = SampleClaim.getCitizenClaim();
    private static final CCDCase ccdCase = CCDCase.builder().previousServiceCaseReference("OCMC00001").build();

    @BeforeEach
    public void setUp() {
        User user = new User("123", null);
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(user);
        when(ccdCaseApi.getCCDCaseByState(ClaimState.CREATE, user)).thenReturn(asList(ccdCase));
        when(caseMapper.from(ccdCase)).thenReturn(citizenClaim);
        doNothing().when(postClaimOrchestrator).citizenIssueHandler(any(CitizenClaimCreatedEvent.class));
    }

    @Test
    public void shouldIssueCreatedClaims() {
        claimIssueService.issueCreatedClaims();
        verify(postClaimOrchestrator).citizenIssueHandler(citizenClaimCreatedEventCaptor.capture());
        verifyEvent(citizenClaimCreatedEventCaptor.getValue(), "John Rambo", "123", citizenClaim);
    }

    private void verifyEvent(ClaimCreationEvent claimCreationEvent, String name, String authorization, Claim claim) {
        assertEquals(name, claimCreationEvent.getSubmitterName());
        assertEquals(authorization, claimCreationEvent.getAuthorisation());
        assertEquals(claim, claimCreationEvent.getClaim());
    }

}
