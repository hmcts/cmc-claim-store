package uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures;

import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;

import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_FORENAME;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_SURNAME;

public final class SampleUserDetails {
    private Long userId = USER_ID;
    private String userEmail = "user@example.com";


    public static SampleUserDetails builder() {
        return new SampleUserDetails();
    }

    public SampleUserDetails withMail(final String userEmail) {
        this.userEmail = userEmail;
        return this;
    }

    public SampleUserDetails withUserId(final Long userId) {
        this.userId = userId;
        return this;
    }

    public UserDetails build() {
        return new UserDetails(userId, userEmail, SUBMITTER_FORENAME, SUBMITTER_SURNAME);
    }

    public static UserDetails getDefault() {
        return SampleUserDetails.builder().build();
    }
}
