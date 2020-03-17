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
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

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
    private final IdamClient idamClient;

    @Autowired
    public UserService(
        IdamApi idamApi,
        IdamCaseworkerProperties idamCaseworkerProperties,
        Oauth2 oauth2,
        IdamClient idamClient
    ) {
        this.idamApi = idamApi;
        this.idamCaseworkerProperties = idamCaseworkerProperties;
        this.oauth2 = oauth2;
        this.idamClient = idamClient;
    }

    @LogExecutionTime
    public UserDetails getUserDetails(String authorisation) {
        UserInfo userInfo = idamClient.getUserInfo(authorisation);

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

}
