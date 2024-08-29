package uk.gov.hmcts.cmc.claimstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworker;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworkerProperties;
import uk.gov.hmcts.cmc.claimstore.models.idam.*;
import uk.gov.hmcts.cmc.claimstore.requests.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.services.user.UserAuthorisationTokenService;
import uk.gov.hmcts.cmc.claimstore.services.user.UserInfoService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;

import java.util.Base64;

@Component
public class UserService {
    public static final String BEARER = "Bearer ";
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String CODE = "code";
    public static final String BASIC = "Basic ";
    private final IdamApi idamApi;
    private final IdamCaseworkerProperties idamCaseworkerProperties;
    private final Oauth2 oauth2;
    private final UserInfoService userInfoService;
    private final UserAuthorisationTokenService userAuthorisationTokenService;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UserService(
        IdamApi idamApi,
        IdamCaseworkerProperties idamCaseworkerProperties,
        Oauth2 oauth2,
        UserInfoService userInfoService,
        UserAuthorisationTokenService userAuthorisationTokenService
    ) {
        this.idamApi = idamApi;
        this.idamCaseworkerProperties = idamCaseworkerProperties;
        this.oauth2 = oauth2;
        this.userInfoService = userInfoService;
        this.userAuthorisationTokenService = userAuthorisationTokenService;
    }

    @LogExecutionTime
    public UserDetails getUserDetails(String authorisation) {
        logger.info("User info invoked");
        UserInfo userInfo = userInfoService.getUserInfo(authorisation);

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

        String authorisation = userAuthorisationTokenService.getAuthorisationToken(username, password);
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

    public UserInfo getUserInfo(String bearerToken) {
        return userInfoService.getUserInfo(bearerToken);
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
        logger.info("IDAM /o/token invoked for tests.");
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
