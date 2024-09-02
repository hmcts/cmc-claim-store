package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserInfo;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.services.user.CacheableUserInfoService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheableUserInfoServiceTest {
    private static final String AUTHORISATION = "Bearer I am a valid token";

    @Mock
    private IdamApi idamApi;

    @InjectMocks
    private CacheableUserInfoService cacheableUserInfoService;

    @Test
    void findsUserInfoForAuthTokenAndCachesResult() {
        UserInfo userInfo = mock(UserInfo.class);
        when(idamApi.retrieveUserInfo(AUTHORISATION)).thenReturn(userInfo);

        cacheableUserInfoService.getUserInfo(AUTHORISATION);
        verify(idamApi, times(1)).retrieveUserInfo(AUTHORISATION);

        cacheableUserInfoService.getUserInfo(AUTHORISATION);

        verifyNoMoreInteractions(idamApi);
    }
}
