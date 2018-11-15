package uk.gov.hmcts.cmc.domain.utils;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isResponseFullDefenceStatesPaid;

public class ResponseUtilsTest {

    @Test(expected = IllegalArgumentException.class)
    public void isResponseFullDefenceStatesPaidReturnsExceptionWhenResponseIsNotDefined() {
        Claim claim = SampleClaim.builder().build();

        isResponseFullDefenceStatesPaid(claim);
    }

    @Test
    public void isResponseFullDefenceStatesPaidReturnsFalseWhenResponseIsNotFullDefence() {
        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().build())
            .build();

        assertThat(isResponseFullDefenceStatesPaid(claim)).isEqualTo(false);
    }

    @Test
    public void isResponseFullDefenceStatesPaidReturnsFalseWhenResponseIsFullDefenceButDefenceTypeDispute() {
        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation();

        assertThat(isResponseFullDefenceStatesPaid(claim)).isEqualTo(false);
    }

    @Test
    public void isResponseFullDefenceStatesPaidReturnsFalseWhenResponseIsFullDefenceButDefenceTypeAlreadyPaid() {
        Claim claim = SampleClaim.getClaimFullDefenceStatesPaidWithAcceptation();

        assertThat(isResponseFullDefenceStatesPaid(claim)).isEqualTo(true);
    }
}
