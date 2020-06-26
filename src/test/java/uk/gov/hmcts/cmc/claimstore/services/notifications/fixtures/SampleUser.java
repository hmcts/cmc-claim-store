package uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures;

import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;

public class SampleUser {
    private static final String DEFAULT_CLAIMANT_ID = "1";
    private static final String DEFAULT_DEFENDANT_ID = "4";
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

    public static User getDefaultDefendant() {
        return builder()
            .withUserDetails(SampleUserDetails.builder().withUserId(DEFAULT_DEFENDANT_ID).withMail("kk@mm.com").build())
            .build();
    }

    public static User getDefaultClaimant() {
        return builder()
            .withUserDetails(SampleUserDetails.builder().withUserId(DEFAULT_CLAIMANT_ID).withMail("kk@mm.com").build())
            .build();
    }

    public static User getDefaultSolicitor() {
        return builder()
            .withUserDetails(SampleUserDetails.builder().withUserId(DEFAULT_CLAIMANT_ID)
                .withMail("solicitor@liar.com")
                .withRoles("solicitor")
                .build()
            )
            .build();
    }

    public static User getDefaultCaseworker() {
        return builder()
            .withUserDetails(SampleUserDetails.builder().withUserId(DEFAULT_CLAIMANT_ID)
                .withMail("caseworker@work.com")
                .withRoles("caseworker-cmc")
                .build()
            )
            .build();
    }

}
