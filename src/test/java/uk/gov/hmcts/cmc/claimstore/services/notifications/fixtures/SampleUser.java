package uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures;

import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;

public class SampleUser {

    private String authorisation = "Bearer letmein";
    private UserDetails userDetails = SampleUserDetails.getDefault();

    public static SampleUser builder() {
        return new SampleUser();
    }

    public SampleUser withAuthorisation(String authorisation) {
        this.authorisation = authorisation;
        return this;
    }

    public SampleUser withUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
        return this;
    }

    public User build() {
        return new User(authorisation, userDetails);
    }

    public static User getDefault() {
        return SampleUser.builder().build();
    }


}
