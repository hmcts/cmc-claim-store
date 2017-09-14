package uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures;

import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;

import static uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_FORENAME;
import static uk.gov.hmcts.cmc.claimstore.events.utils.sampledata.SampleClaimIssuedEvent.SUBMITTER_SURNAME;

public final class SampleUserDetails {

    private SampleUserDetails() {
        // NO-OP
    }

    public static UserDetails getDefault() {
        return new UserDetails(USER_ID, "user@example.com", SUBMITTER_FORENAME, SUBMITTER_SURNAME);
    }
}
