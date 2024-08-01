package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworkerProperties;
import uk.gov.hmcts.cmc.claimstore.models.idam.*;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.PIN;
import static uk.gov.hmcts.cmc.claimstore.services.UserService.CODE;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    public static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String SUB = "user-idam@reform.local";
    private static final String UID = "user-idam-01";
    private static final String NAME = "User IDAM";
    private static final String GIVEN_NAME = "User";
    private static final String FAMILY_NAME = "IDAM";
    private static final List<String> ROLES = List.of("citizen");
    private static final String AUTHORISATION = "Bearer I am a valid token";
    private static final String USERNAME = "user@idam.net";
    private static final String PASSWORD = "I am a strong password";

    private static final UserInfo userInfo = UserInfo.builder()
        .sub(SUB)
        .uid(UID)
        .name(NAME)
        .givenName(GIVEN_NAME)
        .familyName(FAMILY_NAME)
        .roles(ROLES)
        .build();

    @Mock
    private IdamApi idamApi;
    @Mock
    private IdamCaseworkerProperties idamCaseworkerProperties;
    @Mock
    private Oauth2 oauth2;
    @Mock
    private UserInfoService userInfoService;

    @InjectMocks
    private UserService userService;

    @Test
    void findsUserInfoForAuthToken() {
        userService.getUserInfo(AUTHORISATION);
        verify(userInfoService).getUserInfo(AUTHORISATION);
    }

    @Test
    void findsUserDetailsForAuthToken() {
        when(userInfoService.getUserInfo(AUTHORISATION)).thenReturn(userInfo);

        UserDetails userDetails = userService.getUserDetails(AUTHORISATION);

        verify(userInfoService).getUserInfo(AUTHORISATION);
        verifyUserDetails(userDetails);
    }

    private void verifyUserDetails(UserDetails userDetails) {
        assertThat(userDetails.getEmail()).isEqualTo(SUB);
        assertThat(userDetails.getId()).isEqualTo(UID);
        assertThat(userDetails.getForename()).isEqualTo(GIVEN_NAME);
        assertThat(userDetails.getSurname()).hasValue(FAMILY_NAME);
        assertThat(userDetails.getFullName()).isEqualTo(NAME);
        assertThat(userDetails.getRoles()).isEqualTo(ROLES);
    }

    @Test
    void getAuthorisationTokenForGivenUser() {

        when(idamApi.authenticateUser(anyString(), eq(CODE), any(), any()))
            .thenReturn(new AuthenticateUserResponse(CODE));
        when(idamApi.exchangeTokenForTests(eq(CODE), eq(AUTHORIZATION_CODE), any(), any(), any()))
            .thenReturn(new TokenExchangeResponse("I am a valid token"));

        String authorisation = userService.getAuthorisationTokenForTests(USERNAME, PASSWORD);

        assertThat(authorisation).isEqualTo(AUTHORISATION);
    }

    @Test
    void generatePinShouldGenerateValidPin() {

        when(idamApi.generatePin(any(GeneratePinRequest.class), eq(AUTHORISATION)))
            .thenReturn(new GeneratePinResponse(PIN, UID));

        GeneratePinResponse response = userService.generatePin(USERNAME, AUTHORISATION);

        assertThat(response).isNotNull();
        assertThat(response.getPin()).isEqualTo(PIN);
        assertThat(response.getUserId()).isEqualTo(UID);
    }

    @Test
    void authenticateUserShouldReturnUser() {

        when(idamApi.authenticateUser(anyString(), eq(CODE), any(), any()))
            .thenReturn(new AuthenticateUserResponse(CODE));
        when(idamApi.exchangeTokenForTests(eq(CODE), eq(AUTHORIZATION_CODE), any(), any(), any()))
            .thenReturn(new TokenExchangeResponse("I am a valid token"));
        when(userInfoService.getUserInfo(AUTHORISATION))
            .thenReturn(userInfo);

        User user = userService.authenticateUserForTests(USERNAME, PASSWORD);

        verify(userInfoService).getUserInfo(AUTHORISATION);
        assertThat(user.getAuthorisation()).isEqualTo(AUTHORISATION);
        assertUserDetails(user.getUserDetails());
    }

    private void assertUserDetails(UserDetails userDetails) {
        assertThat(userDetails.getEmail()).isEqualTo(SUB);
        assertThat(userDetails.getId()).isEqualTo(UID);
        assertThat(userDetails.getForename()).isEqualTo(GIVEN_NAME);
        assertThat(userDetails.getSurname()).hasValue(FAMILY_NAME);
        assertThat(userDetails.getFullName()).isEqualTo(NAME);
        assertThat(userDetails.getRoles()).isEqualTo(ROLES);
    }
}
