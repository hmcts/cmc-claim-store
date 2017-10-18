package uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures;

import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;

import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_FORENAME;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_SURNAME;

public final class SampleUserDetails {
    private String userId = USER_ID;
    private String userEmail = "user@example.com";
    private String forename = SUBMITTER_FORENAME;
    private String surname = SUBMITTER_SURNAME;


    public static SampleUserDetails builder() {
        return new SampleUserDetails();
    }

    public SampleUserDetails withMail(final String userEmail) {
        this.userEmail = userEmail;
        return this;
    }

    public SampleUserDetails withUserId(final String userId) {
        this.userId = userId;
        return this;
    }

    public SampleUserDetails withForename(final String forename) {
        this.forename = forename;
        return this;
    }

    public SampleUserDetails withSurname(final String surname) {
        this.surname = surname;
        return this;
    }


    public UserDetails build() {
        return new UserDetails(userId, userEmail, forename, surname);
    }

    public static UserDetails getDefault() {
        return SampleUserDetails.builder().build();
    }
}
