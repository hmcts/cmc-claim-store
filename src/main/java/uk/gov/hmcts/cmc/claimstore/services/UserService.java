package uk.gov.hmcts.cmc.claimstore.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworker;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworkerProperties;
import uk.gov.hmcts.cmc.claimstore.models.idam.*;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Component
public class UserService {
    public static final String BEARER = "Bearer ";
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String CODE = "code";
    public static final String BASIC = "Basic ";
    public static final String GRANT_TYPE_PASSWORD = "password";
    public static final String DEFAULT_SCOPE = "openid profile roles";
    private final IdamApi idamApi;
    private final IdamCaseworkerProperties idamCaseworkerProperties;
    private final Oauth2 oauth2;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Cache<String, String> authTokenCache = Caffeine.newBuilder()
        .expireAfterWrite(2, TimeUnit.HOURS)
        .build();
    private final Cache<String, UserInfo> userInfoCache = Caffeine.newBuilder()
        .expireAfterWrite(2, TimeUnit.HOURS)
        .build();

    @Autowired
    public UserService(
        IdamApi idamApi,
        IdamCaseworkerProperties idamCaseworkerProperties,
        Oauth2 oauth2
    ) {
        this.idamApi = idamApi;
        this.idamCaseworkerProperties = idamCaseworkerProperties;
        this.oauth2 = oauth2;
    }

    @LogExecutionTime
    public UserDetails getUserDetails(String authorisation) {
        logger.info("User info invoked");
        UserInfo userInfo = getUserInfo(authorisation);

        return UserDetails.builder()
            .id(userInfo.getUid())
            .email(userInfo.getSub())
            .forename(userInfo.getGivenName())
            .surname(userInfo.getFamilyName())
            .roles(userInfo.getRoles())
            .build();
    }

    @LogExecutionTime
    public User getUser(String authorisation) {
        return new User(authorisation, getUserDetails(authorisation));
    }

    public User authenticateUser(String username, String password) {

        String authorisation = getAuthorisationToken(username, password);
        UserDetails userDetails = getUserDetails(authorisation);
        return new User(authorisation, userDetails);
    }

    @LogExecutionTime
    public User authenticateAnonymousCaseWorker() {
        IdamCaseworker anonymousCaseworker = idamCaseworkerProperties.getAnonymous();
        return authenticateUser(anonymousCaseworker.getUsername(), anonymousCaseworker.getPassword());
    }

    @LogExecutionTime
    public GeneratePinResponse generatePin(String name, String authorisation) {
        return idamApi.generatePin(new GeneratePinRequest(name), authorisation);
    }

    @LogExecutionTime
    public String getAuthorisationToken(String username, String password) {
        logger.info("IDAM /o/token invoked.");
        String authToken = authTokenCache.getIfPresent(username);

        if (authToken == null) {
            TokenExchangeResponse tokenExchangeResponse = idamApi.exchangeToken(
                oauth2.getClientId(),
                oauth2.getClientSecret(),
                oauth2.getRedirectUrl(),
                GRANT_TYPE_PASSWORD,
                username,
                password,
                DEFAULT_SCOPE);
            authToken = BEARER + tokenExchangeResponse.getAccessToken();
            authTokenCache.put(username, authToken);
        }

        return authToken;
    }

    @LogExecutionTime
    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String bearerToken) {
        logger.info("IDAM /o/userinfo invoked");
        UserInfo userInfo = userInfoCache.getIfPresent(bearerToken);

        if (userInfo == null) {
            userInfo = idamApi.retrieveUserInfo(bearerToken);
            userInfoCache.put(bearerToken, userInfo);
        }
        return userInfo;
    }

    public User authenticateUserForTests(String username, String password) {
        String authorisation = getAuthorisationTokenForTests(username, password);
        UserDetails userDetails = getUserDetails(authorisation);
        return new User(authorisation, userDetails);
    }

    public String getAuthorisationTokenForTests(String username, String password) {
        String authorisation = username + ":" + password;
        String base64Authorisation = Base64.getEncoder().encodeToString(authorisation.getBytes());
        AuthenticateUserResponse authenticateUserResponse = idamApi.authenticateUser(
            BASIC + base64Authorisation,
            CODE,
            oauth2.getClientId(),
            oauth2.getRedirectUrl()
        );
        logger.info("IDAM /o/token invoked.");
        TokenExchangeResponse tokenExchangeResponse = idamApi.exchangeTokenForTests(
            authenticateUserResponse.getCode(),
            AUTHORIZATION_CODE,
            oauth2.getRedirectUrl(),
            oauth2.getClientId(),
            oauth2.getClientSecret()
        );
        return BEARER + tokenExchangeResponse.getAccessToken();
    }
}
