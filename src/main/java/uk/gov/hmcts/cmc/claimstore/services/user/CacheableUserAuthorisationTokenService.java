package uk.gov.hmcts.cmc.claimstore.services.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.models.idam.Oauth2;
import uk.gov.hmcts.cmc.claimstore.models.idam.TokenExchangeResponse;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;

@Component
@ConditionalOnProperty(prefix = "idam.user.token.cache", name = "enabled", havingValue = "true")
public class CacheableUserAuthorisationTokenService implements IUserAuthorisationTokenService {
    public static final String GRANT_TYPE_PASSWORD = "password";
    public static final String DEFAULT_SCOPE = "openid profile roles";
    public static final String BEARER = "Bearer ";

    private final IdamApi idamApi;
    private final Oauth2 oauth2;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public CacheableUserAuthorisationTokenService(IdamApi idamApi, Oauth2 oauth2) {
        this.idamApi = idamApi;
        this.oauth2 = oauth2;
    }

    @Override
    @LogExecutionTime
    @Cacheable(value = "userOIDTokenCache")
    public String getAuthorisationToken(String username, String password) {
        logger.info("IDAM /o/token invoked.");
        TokenExchangeResponse tokenExchangeResponse = idamApi.exchangeToken(
            oauth2.getClientId(),
            oauth2.getClientSecret(),
            oauth2.getRedirectUrl(),
            GRANT_TYPE_PASSWORD,
            username,
            password,
            DEFAULT_SCOPE);
        return BEARER + tokenExchangeResponse.getAccessToken();
    }
}
