package uk.gov.hmcts.cmc.ccd.migration.idam.models;

public class User {
    private final String authorisation;
    private final UserDetails userDetails;

    public User(String authorisation, UserDetails userDetails) {
        this.authorisation = authorisation;
        this.userDetails = userDetails;
    }

    public String getAuthorisation() {
        return authorisation;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }
}
