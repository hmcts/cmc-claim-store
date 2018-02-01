package uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures;

import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;

import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_FORENAME;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_SURNAME;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

public final class SampleUserDetails {
    private String userId = USER_ID;
    private String userEmail = "user@example.com";
    private String forename = SUBMITTER_FORENAME;
    private String surname = SUBMITTER_SURNAME;


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


    public UserDetails build() {
        return new UserDetails(userId, userEmail, forename, surname);
    }

    public static UserDetails getDefault() {
        return SampleUserDetails.builder().build();
    }

    public static UserDetails getAnonymousCaseWorker() {
        return SampleUserDetails.builder()
            .withUserId("1234")
            .withMail("anonymous@caseworker.com")
            .withForename("Case")
            .withSurname("Worker")
            .build();
    }
}
