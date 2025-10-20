package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferCaseStayedServiceTest {

    protected static final String BEARER_TOKEN = "Bearer letmein";

    private static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withRoles(Role.CASEWORKER.getRole())
        .withUserId(SampleClaim.USER_ID).build();

    private static final User CASEWORKER = new User(BEARER_TOKEN, SampleUserDetails.builder()
        .withRoles(Role.CASEWORKER.getRole()).build());

    private static final Map<String, String> TRUE_SORT_DIRECTION_MAP = Map.of(
        "sortDirection", "asc",
        "page", "1",
        "state", "open"
    );

    private static final Map<String, String> SORT_DIRECTION_MAP =  Map.of(
        "sortDirection", "asc",
        "state", "open"
    );

    private static final Map<String, Object> INTENTION_TO_PROCEED = Map.of(
            "intentionToProceedDeadline", "2021-12-12",
            "id", 1L
        );

    @Mock
    private IdamApi idamApi;

    @Mock
    private UserService userService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private TransferCaseStayedService service;

    @Test
    public void findCasesForTransferShouldInvokeCorrectly() {

        when(userService.authenticateAnonymousCaseWorker()).thenReturn(CASEWORKER);
        when(idamApi.retrieveUserDetails(BEARER_TOKEN)).thenReturn(USER_DETAILS);

        when(coreCaseDataService.getPaginationInfo(
            BEARER_TOKEN,
            USER_DETAILS.getId(),
            SORT_DIRECTION_MAP
           )
        ).thenReturn(1);

        service.findCasesForTransfer();

        verify(userService, atLeastOnce()).authenticateAnonymousCaseWorker();
        verify(idamApi, atLeastOnce()).retrieveUserDetails(BEARER_TOKEN);

    }

    @Test
    public void compareCasesShouldProduceCorrectResultsWhenInvoked() {

        var ccdStayClaim = CCDCase.builder()
            .id(1L)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(INTENTION_TO_PROCEED)
            .build();

        when(coreCaseDataService.searchCases(
            BEARER_TOKEN,
            USER_DETAILS.getId(),
            TRUE_SORT_DIRECTION_MAP
        )).thenReturn(
                List.of(
                        caseDetails
                ));

        when(coreCaseDataService.caseTransferUpdate(
            BEARER_TOKEN,
            ccdStayClaim,
            CaseEvent.STAY_CLAIM
        )).thenReturn(
            caseDetails
        );

        service.compareCases(BEARER_TOKEN, USER_DETAILS.getId(), 1);

        verify(coreCaseDataService, atLeastOnce())
            .caseTransferUpdate(
                BEARER_TOKEN,
                ccdStayClaim,
                CaseEvent.STAY_CLAIM
            );

        verify(coreCaseDataService, atLeastOnce())
            .searchCases(
                BEARER_TOKEN,
                USER_DETAILS.getId(),
                TRUE_SORT_DIRECTION_MAP
            );

    }
}
