package uk.gov.hmcts.cmc.ccd.migration.idam.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.migration.idam.api.IdamApi;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.AuthenticateUserResponse;
import uk.gov.hmcts.cmc.ccd.migration.idam.models.User;
import uk.gov.hmcts.cmc.ccd.migration.idam.properties.IdamCaseworker;
import uk.gov.hmcts.cmc.ccd.migration.idam.properties.IdamCaseworkerProperties;

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

    public User getUser(String authorisation) {
        return new User(authorisation, idamApi.retrieveUserDetails(authorisation));
    }

    public String authenticateUser(String username, String password) {
        AuthenticateUserResponse authenticateUserResponse = idamApi
            .authenticateUser(getBasicAuthHeader(username, password));

        return BEARER + authenticateUserResponse.getAccessToken();
    }

    public String authenticateSystemUpdateUser() {
        IdamCaseworker systemUpdateUser = idamCaseworkerProperties.getSystemUpdateUser();
        return authenticateUser(systemUpdateUser.getUsername(), systemUpdateUser.getPassword());
    }

    public String authenticateAnonymousCaseworker() {
        IdamCaseworker anonymousCaseworker = idamCaseworkerProperties.getAnonymousCaseworker();
        return authenticateUser(anonymousCaseworker.getUsername(), anonymousCaseworker.getPassword());
    }

    private String getBasicAuthHeader(String username, String password) {
        String auth = username + ":" + password;
        return "Basic " + new String(Base64.getEncoder().encode(auth.getBytes()));
    }

}
