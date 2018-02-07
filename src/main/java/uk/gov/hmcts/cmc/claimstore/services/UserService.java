package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworker;
import uk.gov.hmcts.cmc.claimstore.config.properties.idam.IdamCaseworkerProperties;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;
import uk.gov.hmcts.cmc.claimstore.idam.models.AuthenticateUserResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinRequest;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;

import java.util.Base64;

@Component
public class UserService {

    private static final String BEARER = "Bearer ";
    private final IdamApi idamApi;
    private final IdamCaseworkerProperties idamCaseworkerProperties;

    @Autowired
    public UserService(IdamApi idamApi, IdamCaseworkerProperties idamCaseworkerProperties) {
        this.idamApi = idamApi;
        this.idamCaseworkerProperties = idamCaseworkerProperties;
    }

    public UserDetails getUserDetails(String authorisation) {
        return idamApi.retrieveUserDetails(authorisation);
    }

    public User getUser(String authorisation) {
        return new User(authorisation, idamApi.retrieveUserDetails(authorisation));
    }

    public User authenticateUser(String username, String password) {
        AuthenticateUserResponse authenticateUserResponse = idamApi
            .authenticateUser(getBasicAuthHeader(username, password));

        String authorisation = BEARER + authenticateUserResponse.getAccessToken();
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

    private String getBasicAuthHeader(String username, String password) {
        String auth = username + ":" + password;
        return "Basic " + new String(Base64.getEncoder().encode(auth.getBytes()));
    }

}
