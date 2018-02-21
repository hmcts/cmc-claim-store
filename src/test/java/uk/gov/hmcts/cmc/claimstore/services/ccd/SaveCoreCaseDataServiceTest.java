package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.UserId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SaveCoreCaseDataServiceTest {

    private static final String USER_AUTHORISATION = "Bearer UserAuthorisationToken";
    private static final String CASEWORKER_AUTHORISATION = "Bearer CaseworkerAuthorisationToken";
    private static final String SERVICE_AUTHORISATION = "Bearer ServiceAuthorisationToken";

    private static final String LETTER_HOLDER_ID = "12345";

    private static final String USER_ID = "54321";
    private static final String EVENT_ID = "06245";


    @Mock(answer = Answers.RETURNS_MOCKS)
    private CoreCaseDataApi coreCaseDataApi;
    @Mock(answer = Answers.RETURNS_MOCKS)
    private CaseAccessApi caseAccessApi;
    @Mock(answer = Answers.RETURNS_MOCKS)
    private UserService userService;

    private AuthTokenGenerator authTokenGenerator = () -> SERVICE_AUTHORISATION;

    @Mock
    private Object data;

    private EventRequestData eventRequestData;

    @Captor
    private ArgumentCaptor<CaseDataContent> caseDataContentArgument;
    @Captor
    private ArgumentCaptor<UserId> userIdArgument;

    private SaveCoreCaseDataService service;

    @Before
    public void beforeEachTest() {
        eventRequestData = EventRequestData.builder()
            .userId(USER_ID)
            .jurisdictionId(CCDCaseApi.JURISDICTION_ID)
            .caseTypeId(CCDCaseApi.CASE_TYPE_ID)
            .eventId(EVENT_ID)
            .ignoreWarning(false)
            .build();
        service = new SaveCoreCaseDataService(coreCaseDataApi, authTokenGenerator, caseAccessApi, userService);
    }

    @Test
    public void shouldStartForCitizenEventForNonRepresentedClaim() {
        service.save(USER_AUTHORISATION, eventRequestData, data, false, LETTER_HOLDER_ID);

        verify(coreCaseDataApi).startForCitizen(
            eq(USER_AUTHORISATION),
            eq(SERVICE_AUTHORISATION),
            eq(USER_ID),
            eq(CCDCaseApi.JURISDICTION_ID),
            eq(CCDCaseApi.CASE_TYPE_ID),
            eq(EVENT_ID)
        );
    }

    @Test
    public void shouldStartForCaseworkerEventForRepresentedClaim() {
        service.save(USER_AUTHORISATION, eventRequestData, data, true, LETTER_HOLDER_ID);

        verify(coreCaseDataApi).startForCaseworker(
            eq(USER_AUTHORISATION),
            eq(SERVICE_AUTHORISATION),
            eq(USER_ID),
            eq(CCDCaseApi.JURISDICTION_ID),
            eq(CCDCaseApi.CASE_TYPE_ID),
            eq(EVENT_ID)
        );
    }

    @Test
    public void shouldSubmitForCitizenForNonRepresentedClaim() {
        service.save(USER_AUTHORISATION, eventRequestData, data, false, LETTER_HOLDER_ID);

        verify(coreCaseDataApi).submitForCitizen(
            eq(USER_AUTHORISATION),
            eq(SERVICE_AUTHORISATION),
            eq(USER_ID),
            eq(CCDCaseApi.JURISDICTION_ID),
            eq(CCDCaseApi.CASE_TYPE_ID),
            eq(false),
            caseDataContentArgument.capture()
        );

        assertThat(caseDataContentArgument.getValue().getData()).isEqualTo(data);
    }

    @Test
    public void shouldSubmitForCaseworkerForRepresentedClaim() {
        service.save(USER_AUTHORISATION, eventRequestData, data, true, LETTER_HOLDER_ID);

        verify(coreCaseDataApi).submitForCaseworker(
            eq(USER_AUTHORISATION),
            eq(SERVICE_AUTHORISATION),
            eq(USER_ID),
            eq(CCDCaseApi.JURISDICTION_ID),
            eq(CCDCaseApi.CASE_TYPE_ID),
            eq(false),
            caseDataContentArgument.capture()
        );

        assertThat(caseDataContentArgument.getValue().getData()).isEqualTo(data);
    }

    @Test
    public void shouldGrantLetterHolderAccessToCaseForNonRepresentedClaim() {
        User anonCaseworker = new User(CASEWORKER_AUTHORISATION, SampleUserDetails.getDefault());
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(anonCaseworker);

        service.save(USER_AUTHORISATION, eventRequestData, data, false, LETTER_HOLDER_ID);

        verify(caseAccessApi).grantAccessToCase(
            eq(CASEWORKER_AUTHORISATION),
            eq(SERVICE_AUTHORISATION),
            eq(anonCaseworker.getUserDetails().getId()),
            eq(CCDCaseApi.JURISDICTION_ID),
            eq(CCDCaseApi.CASE_TYPE_ID),
            anyString(),
            userIdArgument.capture()
        );

        assertThat(userIdArgument.getValue().getId()).isEqualTo(LETTER_HOLDER_ID);
    }

    @Test
    public void shouldNotGrantLetterHolderAccessToCaseForRepresentedClaim() {
        service.save(USER_AUTHORISATION, eventRequestData, data, true, LETTER_HOLDER_ID);

        verifyZeroInteractions(caseAccessApi);
    }

}
