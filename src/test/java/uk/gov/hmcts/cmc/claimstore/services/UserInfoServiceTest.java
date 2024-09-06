package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.services.user.UserInfoService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserInfoServiceTest {
    private static final String AUTHORISATION = "Bearer I am a valid token";

    @Mock
    private IdamApi idamApi;
    @InjectMocks
    private UserInfoService userInfoService;

    @Test
    void findsUserInfoForAuthToken() {
        userInfoService.getUserInfo(AUTHORISATION);
        verify(idamApi).retrieveUserInfo(AUTHORISATION);
    }
}
