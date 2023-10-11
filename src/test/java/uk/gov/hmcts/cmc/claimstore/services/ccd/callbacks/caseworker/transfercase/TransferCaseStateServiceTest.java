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
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CoreCaseDataService;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransferCaseStateServiceTest {

    protected static final String AUTHORIZATION = "Auth token";

    private static final UserDetails USER_DETAILS = new UserDetails(
        "id", "email", "forename", "surname", emptyList());

    protected static final String BEARER_TOKEN = "Bearer letmein";

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransferCaseStateService transferCaseStateService;

    @Test
    public void casesShouldTransferWhenGivenAState() {

        when(userService.authenticateAnonymousCaseWorker())
            .thenReturn(new User(AUTHORIZATION, USER_DETAILS));

        var ccdCase = CCDCase.builder()
            .id(1L)
            .build();

        transferCaseStateService.transferCaseToGivenCaseState(CaseEvent.TRANSFER, ccdCase.getId());

        verify(coreCaseDataService, atLeastOnce())
            .caseTransferUpdate(
                AUTHORIZATION,
                ccdCase,
                CaseEvent.TRANSFER
            );
    }
}
