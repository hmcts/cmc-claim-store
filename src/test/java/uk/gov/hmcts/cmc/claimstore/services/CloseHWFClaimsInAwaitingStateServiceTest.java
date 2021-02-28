package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.CloseHWFClaimsInAwaitingStateService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDateTime;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CLOSE_AWAITING_RESPONSE_HWF;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.AWAITING_RESPONSE_HWF;

@RunWith(MockitoJUnitRunner.class)
public class CloseHWFClaimsInAwaitingStateServiceTest {

    @InjectMocks
    private CloseHWFClaimsInAwaitingStateService closeHWFClaimsInAwaitingStateService;

    @Mock
    private CaseMapper caseMapper;
    @Mock
    private UserService userService;
    @Mock
    private ClaimService claimService;
    @Mock
    private CoreCaseDataService coreCaseDataService;

    private static final String AUTHORISATION = "authorisation";
    private static final User USER = new User(AUTHORISATION, null);
    private static final Claim SAMPLE_CLAIM = SampleClaim.getCitizenClaim();

    @Before
    public void setUp() {
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(USER);
    }

    @Test
    public void shouldCloseClaimIfWaitingResponseForMoreThan95Days() {
        Claim claimMoreThan95DaysOld = SAMPLE_CLAIM.toBuilder().lastModified(LocalDateTime.now().minusDays(96)).build();
        when(claimService.getClaimsByState(AWAITING_RESPONSE_HWF, USER)).thenReturn(asList(claimMoreThan95DaysOld));
        closeHWFClaimsInAwaitingStateService.findCasesAndClose();
        verify(coreCaseDataService).update(AUTHORISATION, caseMapper.to(SAMPLE_CLAIM), CLOSE_AWAITING_RESPONSE_HWF);
    }

    @Test
    public void shouldNotCloseClaimIfWaitingResponseForMoreThan95Days() {
        Claim claimLessThan95DaysOld = SAMPLE_CLAIM.toBuilder().lastModified(LocalDateTime.now().minusDays(94)).build();
        when(claimService.getClaimsByState(AWAITING_RESPONSE_HWF, USER)).thenReturn(asList(claimLessThan95DaysOld));
        closeHWFClaimsInAwaitingStateService.findCasesAndClose();
        verify(coreCaseDataService, times(0)).update(AUTHORISATION,
            caseMapper.to(SAMPLE_CLAIM), CLOSE_AWAITING_RESPONSE_HWF);
    }

}
