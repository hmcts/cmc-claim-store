package uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures;

import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_FORENAME;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_SURNAME;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

public final class SampleUserDetails {
    private String userId = USER_ID;
    private String userEmail = "user@example.com";
    private String forename = SUBMITTER_FORENAME;
    private String surname = SUBMITTER_SURNAME;
    private List<String> roles = Collections.singletonList("citizen");

    public static SampleUserDetails builder() {
        return new SampleUserDetails();
    }

    public SampleUserDetails withMail(String userEmail) {
        this.userEmail = userEmail;
        return this;
    }

    public SampleUserDetails withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public SampleUserDetails withForename(String forename) {
        this.forename = forename;
        return this;
    }

    public SampleUserDetails withSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public SampleUserDetails withRoles(String... roles) {
        this.roles = Arrays.asList(roles);
        return this;
    }

    public UserDetails build() {
        return new UserDetails(userId, userEmail, forename, surname, roles);
    }

    public static UserDetails getDefault() {
        return SampleUserDetails.builder().build();
    }
}
