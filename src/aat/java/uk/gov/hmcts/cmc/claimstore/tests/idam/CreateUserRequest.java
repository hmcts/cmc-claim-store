package uk.gov.hmcts.cmc.claimstore.tests.idam;

import java.util.List;

public class CreateUserRequest {

    private final String email;
    private final String forename = "John";
    private final String surname = "Smith";
    private final Integer levelOfAccess = 0;
    private final List<UserRole> roles;
    private final String activationDate = "";
    private final String lastAccess = "";
    private final String password;

    public CreateUserRequest(
        String email,
        List<UserRole> roles,
        String password
    ) {
        this.email = email;
        this.roles = roles;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getForename() {
        return forename;
    }

    public String getSurname() {
        return surname;
    }

    public Integer getLevelOfAccess() {
        return levelOfAccess;
    }

    public List<UserRole> getRoles() {
        return roles;
    }

    public String getActivationDate() {
        return activationDate;
    }

    public String getLastAccess() {
        return lastAccess;
    }

    public String getPassword() {
        return password;
    }

}
