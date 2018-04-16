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
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;

import java.util.Base64;

@Component
public class UserService {

    private static final String BEARER = "Bearer ";
    private final IdamApi idamApi;
    private final IdamCaseworkerProperties idamCaseworkerProperties;
    private final Oauth2 oauth2;

    @Autowired
    public UserService(IdamApi idamApi, IdamCaseworkerProperties idamCaseworkerProperties, Oauth2 oauth2) {
        this.idamApi = idamApi;
        this.idamCaseworkerProperties = idamCaseworkerProperties;
        this.oauth2 = oauth2;
    }

    public UserDetails getUserDetails(String authorisation) {
        return idamApi.retrieveUserDetails(authorisation);
    }

    public User getUser(String authorisation) {
        return new User(authorisation, idamApi.retrieveUserDetails(authorisation));
    }

    public User authenticateUser(String username, String password) {

        String authorisation = getIdamOauth2Token(username, password);
        UserDetails userDetails = idamApi.retrieveUserDetails(authorisation);
        return new User(authorisation, userDetails);
    }

    public User authenticateAnonymousCaseWorker() {
        IdamCaseworker anonymousCaseworker = idamCaseworkerProperties.getAnonymousCaseworker();
        return authenticateUser(anonymousCaseworker.getUsername(), anonymousCaseworker.getPassword());
    }

    public GeneratePinResponse generatePin(String name, String authorisation) {
        return idamApi.generatePin(new GeneratePinRequest(name), authorisation);
    }

    public String getBasicAuthHeader(String username, String password) {
        String auth = username + ":" + password;
        return "Basic " + new String(Base64.getEncoder().encode(auth.getBytes()));
    }

    public String getIdamOauth2Token(String username, String password) {
        String authorisation = username + ":" + password;
        String base64Authorisation = Base64.getEncoder().encodeToString(authorisation.getBytes());

        AuthenticateUserResponse authorize = idamApi.authorizeCodeType(
            "Basic " + base64Authorisation,
            "code",
            oauth2.getClientId(),
            oauth2.getRedirectUrl()
        );

        AuthenticateUserResponse authorizeToken = idamApi.authorizeToken(
            authorize.getCode(),
            "authorization_code",
            oauth2.getRedirectUrl(),
            oauth2.getClientId(),
            oauth2.getClientSecret()
        );

        return "Bearer " + authorizeToken.getAccessToken();
    }

}
