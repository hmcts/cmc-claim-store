package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.models.idam.Oauth2;
import uk.gov.hmcts.cmc.claimstore.models.idam.TokenExchangeResponse;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.services.user.CacheableUserAuthorisationTokenService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheableUserAuthorisationTokenServiceTest {
    private static final String TOKEN = "I am a valid token";
    public static final String BEARER = "Bearer ";
    private static final String USERNAME = "Username";
    private static final String PASSWORD = "Password";

    @Mock
    private IdamApi idamApi;
    @Mock
    private Oauth2 oauth2;
    @InjectMocks
    private CacheableUserAuthorisationTokenService cacheableUserAuthorisationTokenService;

    @Mock
    TokenExchangeResponse tokenExchangeResponse;

    @Test
    void findsUserInfoForAuthTokenAndCachesResult() {
        when(idamApi.exchangeToken(
            any(),
            any(),
            any(),
            any(),
            eq(USERNAME),
            eq(PASSWORD),
            any())
        ).thenReturn(tokenExchangeResponse);
        when(tokenExchangeResponse.getAccessToken()).thenReturn(TOKEN);

        final String authorisationToken = cacheableUserAuthorisationTokenService.getAuthorisationToken(USERNAME, PASSWORD);
        assertEquals(BEARER + TOKEN, authorisationToken);
        verify(idamApi, times(1)).exchangeToken(
            any(),
            any(),
            any(),
            any(),
            eq(USERNAME),
            eq(PASSWORD),
            any()
        );

        final String cachedAuthorisationToken = cacheableUserAuthorisationTokenService.getAuthorisationToken(USERNAME, PASSWORD);
        assertEquals(BEARER + TOKEN, cachedAuthorisationToken);

        verifyNoMoreInteractions(idamApi);
    }
}
