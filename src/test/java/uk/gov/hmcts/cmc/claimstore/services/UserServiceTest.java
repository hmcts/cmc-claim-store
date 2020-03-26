package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworkerProperties;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.idam.models.AuthenticateUserResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinRequest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.Oauth2;
import uk.gov.hmcts.cmc.claimstore.idam.models.TokenExchangeResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.UserService.AUTHORIZATION_CODE;
import static uk.gov.hmcts.cmc.claimstore.services.UserService.CODE;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    private static final String SUB = "user-idam@reform.local";
    private static final String UID = "user-idam-01";
    private static final String NAME = "User IDAM";
    private static final String GIVEN_NAME = "User";
    private static final String FAMILY_NAME = "IDAM";
    private static final String PIN = "ABCD";
    private static final List<String> ROLES = Lists.newArrayList("citizen");

    private static final String USERNAME = "user@idam.net";
    private static final String PASSWORD = "I am a strong password";
    private static final String AUTHORISATION = "Bearer I am a valid token";

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

    private UserService userService;

    @Before
    public void setup() {
        userService = new UserService(idamApi, idamCaseworkerProperties, oauth2);
        when(idamApi.retrieveUserInfo(eq(AUTHORISATION))).thenReturn(userInfo);
    }

    @Test
    public void findsUserInfoForAuthToken() {

        UserInfo found = userService.getUserInfo(AUTHORISATION);

        assertThat(found.getSub()).isEqualTo(SUB);
        assertThat(found.getUid()).isEqualTo(UID);
        assertThat(found.getName()).isEqualTo(NAME);
        assertThat(found.getGivenName()).isEqualTo(GIVEN_NAME);
        assertThat(found.getFamilyName()).isEqualTo(FAMILY_NAME);
        assertThat(found.getRoles()).isEqualTo(ROLES);
    }

    @Test
    public void findsUserDetailsForAuthToken() {

        UserDetails userDetails = userService.getUserDetails(AUTHORISATION);

        assertUserDetails(userDetails);
    }

    private void assertUserDetails(UserDetails userDetails) {
        assertThat(userDetails.getEmail()).isEqualTo(SUB);
        assertThat(userDetails.getId()).isEqualTo(UID);
        assertThat(userDetails.getForename()).isEqualTo(GIVEN_NAME);
        assertThat(userDetails.getSurname()).hasValue(FAMILY_NAME);
        assertThat(userDetails.getFullName()).isEqualTo(NAME);
        assertThat(userDetails.getRoles()).isEqualTo(ROLES);
    }

    @Test
    public void getAuthorisationTokenForGivenUser() {

        when(idamApi.authenticateUser(anyString(), eq(CODE), any(), any()))
            .thenReturn(new AuthenticateUserResponse(CODE));
        when(idamApi.exchangeToken(eq(CODE), eq(AUTHORIZATION_CODE), any(), any(), any()))
            .thenReturn(new TokenExchangeResponse("I am a valid token"));

        String authorisation = userService.getAuthorisationToken(USERNAME, PASSWORD);

        assertThat(authorisation).isEqualTo(AUTHORISATION);
    }

    @Test
    public void generatePinShouldGenerateValidPin() {

        when(idamApi.generatePin(any(GeneratePinRequest.class), eq(AUTHORISATION)))
            .thenReturn(new GeneratePinResponse(PIN, UID));

        GeneratePinResponse response = userService.generatePin(USERNAME, AUTHORISATION);

        assertThat(response).isNotNull();
        assertThat(response.getPin()).isEqualTo(PIN);
        assertThat(response.getUserId()).isEqualTo(UID);
    }

    @Test
    public void authenticateUserShouldReturnUser() {

        when(idamApi.authenticateUser(anyString(), eq(CODE), any(), any()))
            .thenReturn(new AuthenticateUserResponse(CODE));
        when(idamApi.exchangeToken(eq(CODE), eq(AUTHORIZATION_CODE), any(), any(), any()))
            .thenReturn(new TokenExchangeResponse("I am a valid token"));

        User user = userService.authenticateUser(USERNAME, PASSWORD);

        assertThat(user.getAuthorisation()).isEqualTo(AUTHORISATION);
        assertUserDetails(user.getUserDetails());
    }
}
