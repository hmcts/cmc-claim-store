package uk.gov.hmcts.cmc.dm.batch.idam.models;

public class User {
    private String authorisation;
    private UserDetails userDetails;

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
