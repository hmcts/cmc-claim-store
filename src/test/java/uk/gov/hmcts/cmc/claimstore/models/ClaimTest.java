package uk.gov.hmcts.cmc.claimstore.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.RESPONSE_DEADLINE;

public class ClaimTest {

    @Test
    public void isEqualWhenTheSameObjectWithDefaultValuesIsGiven() {
        Claim claim = SampleClaim.getDefault();
        assertThat(claim).isEqualTo(claim);
    }

    @Test
    public void isEqualWhenTheSameObjectWithCustomValuesIsGiven() {
        Claim claim = customValues();

        assertThat(claim).isEqualTo(claim);
    }

    @Test
    public void isNotEqualWhenNullGiven() {
        Claim claim = SampleClaim.getDefault();
        assertThat(claim).isNotEqualTo(null);
    }

    @Test
    public void isNotEqualWhenDifferentTypeObjectGiven() {
        Claim claim = SampleClaim.getDefault();
        assertThat(claim).isNotEqualTo(new HashMap<>());
    }

    @Test
    public void isNotEqualWhenDifferentClaimObjectGiven() {
        Claim claim1 = customValues();
        Claim claim2 = SampleClaim.getDefault();
        assertThat(claim1).isNotEqualTo(claim2);
    }

    @Test
    public void isNotEqualWhenOnlyOneFieldIsDifferent() {
        Claim claim1 = customCreatedAt(NOW_IN_LOCAL_ZONE.plusNanos(1));
        Claim claim2 = customCreatedAt(NOW_IN_LOCAL_ZONE);
        assertThat(claim1).isNotEqualTo(claim2);
    }

    private static Claim customValues() {
        return customCreatedAt(NOW_IN_LOCAL_ZONE);
    }

    private static Claim customCreatedAt(final LocalDateTime createdAt) {
        return new Claim(
            1L,
            2L,
            3L,
            4L,
            "external-id",
            "ref number",
            null,
            createdAt,
            ISSUE_DATE,
            RESPONSE_DEADLINE,
            false,
            "claimant@mail.com",
            null,
            null, null);
    }
}
