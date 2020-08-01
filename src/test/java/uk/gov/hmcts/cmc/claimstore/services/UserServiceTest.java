package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworkerProperties;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.idam.models.Oauth2;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

    @BeforeEach
    void setup() {
        userService = new UserService(idamApi, idamCaseworkerProperties, oauth2);
    }

    @Test
    void findsUserInfoForAuthToken() {
        when(idamApi.retrieveUserInfo(AUTHORISATION)).thenReturn(userInfo);

        UserInfo found = userService.getUserInfo(AUTHORISATION);

        assertThat(found.getSub()).isEqualTo(SUB);
        assertThat(found.getUid()).isEqualTo(UID);
        assertThat(found.getName()).isEqualTo(NAME);
        assertThat(found.getGivenName()).isEqualTo(GIVEN_NAME);
        assertThat(found.getFamilyName()).isEqualTo(FAMILY_NAME);
        assertThat(found.getRoles()).isEqualTo(ROLES);
    }

    @Test
    void findsUserDetailsForAuthToken() {
        when(idamApi.retrieveUserInfo(AUTHORISATION)).thenReturn(userInfo);

        UserDetails userDetails = userService.getUserDetails(AUTHORISATION);

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
}
