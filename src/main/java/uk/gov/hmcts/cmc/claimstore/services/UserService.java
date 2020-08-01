package uk.gov.hmcts.cmc.claimstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworker;
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
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;

@Component
public class UserService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String BEARER = "Bearer ";
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String CODE = "code";
    public static final String BASIC = "Basic ";

    private final IdamApi idamApi;
    private final IdamCaseworkerProperties idamCaseworkerProperties;
    private final Oauth2 oauth2;
    public static final String GRANT_TYPE_PASSWORD = "password";
    public static final String DEFAULT_SCOPE = "openid roles profile";

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

    public String getAuthorisationToken(String username, String password) {

        AuthenticateUserResponse authenticateUserResponse = idamApi.authenticateUser(
            oauth2.getClientId(), oauth2.getClientSecret(), oauth2.getRedirectUrl(),
            GRANT_TYPE_PASSWORD, username, password, "openid profile roles"
        );
        logger.info("IDAM /o/token invoked.");
        TokenExchangeResponse tokenExchangeResponse = idamApi.exchangeToken(
            authenticateUserResponse.getCode(),
            GRANT_TYPE_PASSWORD,
            oauth2.getRedirectUrl(),
            oauth2.getClientId(),
            oauth2.getClientSecret()
        );

        return BEARER + tokenExchangeResponse.getAccessToken();
    }

    @LogExecutionTime
    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String bearerToken) {
        logger.info("IDAM /o/userinfo invoked");
        return idamApi.retrieveUserInfo(bearerToken);
    }

}
