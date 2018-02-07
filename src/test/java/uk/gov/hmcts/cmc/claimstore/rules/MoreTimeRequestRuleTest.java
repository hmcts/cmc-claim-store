package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeRequestedAfterDeadlineException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;

public class MoreTimeRequestRuleTest {

    private final MoreTimeRequestRule moreTimeRequestRule = new MoreTimeRequestRule();

    @Test
    public void shouldNotThrowExceptionWhenMoreTimeRequested() {
        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().plusDays(2))
            .withMoreTimeRequested(false)
            .build();
        moreTimeRequestRule.assertMoreTimeCanBeRequested(claim);
    }

    @Test(expected = MoreTimeAlreadyRequestedException.class)
    public void shouldThrowExceptionWhenMoreTimeWasAlreadyResponded() {
        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().plusDays(2))
            .withMoreTimeRequested(true)
            .build();
        moreTimeRequestRule.assertMoreTimeCanBeRequested(claim);
    }

    @Test(expected = MoreTimeRequestedAfterDeadlineException.class)
    public void shouldThrowExceptionWhenResponseDeadlinePassed() {
        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusDays(1))
            .withMoreTimeRequested(false)
            .build();
        moreTimeRequestRule.assertMoreTimeCanBeRequested(claim);
    }
}
