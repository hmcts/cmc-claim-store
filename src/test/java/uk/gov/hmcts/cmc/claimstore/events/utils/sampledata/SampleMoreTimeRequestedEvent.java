package uk.gov.hmcts.cmc.claimstore.events.utils.sampledata;

import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;

public class SampleMoreTimeRequestedEvent {

    public static final String DEFENDANT_EMAIL = "defendant@example.com";
    public static final LocalDate NEW_RESPONSE_DEADLINE = LocalDate.now().plusDays(14);
    public static final Claim CLAIM = SampleClaim.getDefault();

    private SampleMoreTimeRequestedEvent() {
        // don't instantiate
    }

    public static MoreTimeRequestedEvent getDefault() {
        return new MoreTimeRequestedEvent(CLAIM, NEW_RESPONSE_DEADLINE, DEFENDANT_EMAIL);
    }

    public static String getReference(final String toWhom, final String claimReferenceNumber) {
        return String.format("more-time-requested-notification-to-%s-%s", toWhom, claimReferenceNumber);
    }
}
