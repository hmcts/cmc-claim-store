package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserInfo;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUser;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransferCaseStateServiceTest {

    protected static final String BEARER_TOKEN = "Bearer letmein";

    private static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withRoles(Role.CASEWORKER.getRole())
        .withUserId(SampleClaim.USER_ID).build();

    private static final User CASEWORKER = new User(BEARER_TOKEN, SampleUserDetails.builder()
        .withRoles(Role.CASEWORKER.getRole()).build());

    private static final String ROOT_PATH = "/claims";

    private static final String AUTHORISATION_TOKEN_CITIZEN = "Bearer letmein";
    private static final UserDetails CITIZEN_DETAILS = SampleUserDetails.builder()
        .withRoles(Role.CITIZEN.getRole())
        .withUserId(SampleClaim.USER_ID).build();
    private static final User CITIZEN = SampleUser.builder().withUserDetails(CITIZEN_DETAILS).build();

    private static final String AUTHORISATION_TOKEN_LEGAL_REP = "Bearer letmein";
    private static final UserDetails LEGAL_REP_DETAILS = SampleUserDetails.builder()
        .withRoles(Role.LEGAL_ADVISOR.getRole())
        .withUserId(SampleClaim.USER_ID).build();
    private static final String RETURN_URL = "http://return.url";
    private static final User LEGAL_REP = new User(BEARER_TOKEN, CITIZEN_DETAILS);
    private static final String REASON = "blah".repeat(4);

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private UserService userService;

    @Mock
    private User user;

    private TransferCaseStateService transferCaseStateService;

    @Before
    public void setUp(){
        given(userService.getUserInfo(anyString())).willReturn(UserInfo.builder()
            .roles(ImmutableList.of(Role.CITIZEN.getRole()))
            .uid(SampleClaim.USER_ID)
            .sub(SampleClaim.SUBMITTER_EMAIL)
            .build());
        given(userService.getUser(AUTHORISATION_TOKEN_CITIZEN)).willReturn(CITIZEN);
        given(userService.getUserDetails(AUTHORISATION_TOKEN_CITIZEN)).willReturn(CITIZEN_DETAILS);

        given(userService.getUser(AUTHORISATION_TOKEN_LEGAL_REP)).willReturn(LEGAL_REP);
        given(userService.getUserDetails(AUTHORISATION_TOKEN_LEGAL_REP)).willReturn(LEGAL_REP_DETAILS);

    }


    @Test
    public void compareCasesShouldProduceCorrectResultsWhenInvoked() {
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(LEGAL_REP);

        transferCaseStateService = new TransferCaseStateService(userService, coreCaseDataService);

        var ccdCase = CCDCase.builder()
            .id(1L)
            .build();

        transferCaseStateService.transferGivenCaseState(CaseEvent.TRANSFER, ccdCase.getId());

        verify(coreCaseDataService, atLeastOnce())
            .caseTransferUpdate(
                BEARER_TOKEN,
                ccdCase,
                CaseEvent.TRANSFER
            );
    }
}
