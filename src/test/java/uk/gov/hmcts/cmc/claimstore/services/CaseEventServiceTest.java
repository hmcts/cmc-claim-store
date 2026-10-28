package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CaseEventServiceTest {

    private CaseEventService caseEventService;

    @Mock
    private CaseEventsApi caseEventsApi;

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private final User user = new User("", new UserDetails(null, null, null, null, null));

    private static final String SERVICE_AUTHORISATION = "122FSDFSFDSFDSFdsafdsfadsfasdfaaa2323232";
    private static final String UUID = "UUID";

    public static final String JURISDICTION_ID = "CMC";
    public static final String CASE_TYPE_ID = "MoneyClaimCase";

    private List<CaseEvent> caseEventList = new ArrayList<>();
    private List<CaseEventDetail> caseEventDetailList = new ArrayList<>();

    @BeforeEach
    public void setUp() {

        caseEventService = new CaseEventService(
            caseEventsApi,
            authTokenGenerator
        );

        caseEventList.add(CaseEvent.CLOSE_AWAITING_RESPONSE_HWF);

        caseEventDetailList.add(CaseEventDetail.builder().id(CaseEvent.CLOSE_AWAITING_RESPONSE_HWF.getValue())
            .eventName(CaseEvent.CLOSE_AWAITING_RESPONSE_HWF.name()).createdDate(LocalDateTime.now()).build());
    }

    @Test
    public void shouldFindEventsForCases() {
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORISATION);
        when(caseEventsApi.findEventDetailsForCase(user.getAuthorisation(), SERVICE_AUTHORISATION,
            user.getUserDetails().getId(),
            JURISDICTION_ID, CASE_TYPE_ID, "1"))
            .thenReturn(caseEventDetailList);
        List<CaseEvent> caseEventListOutput = caseEventService.findEventsForCase("1", user);
        assertThat(caseEventListOutput).isEqualTo(caseEventList);
    }
}
