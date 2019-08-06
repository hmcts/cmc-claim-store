package uk.gov.hmcts.cmc.claimstore.tests.idam;

public class CreateUserRequest {

    private final String email;
    private final String forename = "IDAM";
    private final String surname = "One";
    private final Integer levelOfAccess = 0;
    private final UserGroup userGroup;
    private final String activationDate = "";
    private final String lastAccess = "";
    private final String password;

    public CreateUserRequest(
        String email,
        UserGroup userGroup,
        String password
    ) {
        this.email = email;
        this.userGroup = userGroup;
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

    public UserGroup getUserGroup() {
        return userGroup;
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
