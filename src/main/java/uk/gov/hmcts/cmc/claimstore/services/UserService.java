package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
public class UserService {

    public static final String BEARER = "Bearer ";
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String CODE = "code";
    public static final String BASIC = "Basic ";
    public static final List<String> userRoles = Arrays.asList("cmc-new-features-consent-given",
        "cmc-new-features-consent-not-given");
    private final IdamApi idamApi;
    private final IdamCaseworkerProperties idamCaseworkerProperties;
    private final Oauth2 oauth2;

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
        return idamApi.retrieveUserDetails(authorisation);
    }

    @LogExecutionTime
    public User getUser(String authorisation) {
        return new User(authorisation, getUserDetails(authorisation));
    }

    public User authenticateUser(String username, String password) {

        String authorisation = getIdamOauth2Token(username, password);
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

    public String getBasicAuthHeader(String username, String password) {
        String authorisation = username + ":" + password;
        return BASIC + Base64.getEncoder().encodeToString(authorisation.getBytes());
    }

    public String getIdamOauth2Token(String username, String password) {
        String authorisation = username + ":" + password;
        String base64Authorisation = Base64.getEncoder().encodeToString(authorisation.getBytes());

        AuthenticateUserResponse authenticateUserResponse = idamApi.authenticateUser(
            BASIC + base64Authorisation,
            CODE,
            oauth2.getClientId(),
            oauth2.getRedirectUrl()
        );

        TokenExchangeResponse tokenExchangeResponse = idamApi.exchangeCode(
            authenticateUserResponse.getCode(),
            AUTHORIZATION_CODE,
            oauth2.getRedirectUrl(),
            oauth2.getClientId(),
            oauth2.getClientSecret()
        );

        return BEARER + tokenExchangeResponse.getAccessToken();
    }

    @LogExecutionTime
    public UserInfo getUserInfo(String bearerToken) {
        return idamApi.retrieveUserInfo(bearerToken);
    }

}
