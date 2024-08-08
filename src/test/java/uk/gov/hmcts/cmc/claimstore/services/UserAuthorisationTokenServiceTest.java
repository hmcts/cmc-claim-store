package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.models.idam.Oauth2;
import uk.gov.hmcts.cmc.claimstore.models.idam.TokenExchangeResponse;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthorisationTokenServiceTest {
    private static final String TOKEN = "I am a valid token";
    public static final String BEARER = "Bearer ";
    private static final String USERNAME = "Username";
    private static final String PASSWORD = "Password";

    @Mock
    private IdamApi idamApi;
    @Mock
    private Oauth2 oauth2;
    @InjectMocks
    private UserAuthorisationTokenService userAuthorisationTokenService;

    @Mock
    TokenExchangeResponse tokenExchangeResponse;

    @Test
    void findsUserInfoForAuthToken() {
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

        final String authorisationToken = userAuthorisationTokenService.getAuthorisationToken(USERNAME, PASSWORD);
        assertEquals(BEARER + TOKEN, authorisationToken);
    }
}
