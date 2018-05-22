package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeRequestedAfterDeadlineException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;

public class MoreTimeRequestRuleTest {

    private MoreTimeRequestRule moreTimeRequestRule = new MoreTimeRequestRule(new ClaimDeadlineService());

    @Test
    public void noExceptionThrownWhenMoreTimeRequestedFirstTimeAndDeadlineHasNotPassed() {
        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().plusDays(2))
            .withMoreTimeRequested(false)
            .build();
        assertThatCode(() -> moreTimeRequestRule.assertMoreTimeCanBeRequested(claim)).doesNotThrowAnyException();
    }

    @Test(expected = MoreTimeAlreadyRequestedException.class)
    public void shouldThrowExceptionWhenMoreTimeWasAlreadyRequested() {
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

    @Test
    public void assertIsNotPastDeadlineShouldNotThrowWhenTimeIsBefore4PMOnDeadlineDay() {
        LocalDateTime now = LocalDate.now().atTime(15, 59, 59, 999);
        LocalDate responseDeadline = LocalDate.now();
        assertThatCode(
            () -> moreTimeRequestRule.assertIsNotPastDeadline(now, responseDeadline)
        ).doesNotThrowAnyException();
    }

    @Test(expected = MoreTimeRequestedAfterDeadlineException.class)
    public void assertIsNotPastDeadlineShouldThrowWhenTimeIs4PMOnDeadlineDay() {
        LocalDateTime now = LocalDate.now().atTime(16, 0);
        LocalDate responseDeadline = LocalDate.now();
        moreTimeRequestRule.assertIsNotPastDeadline(now, responseDeadline);
    }

    @Test(expected = MoreTimeRequestedAfterDeadlineException.class)
    public void assertIsNotPastDeadlineShouldThrowWhenTimeIsPast4PMOnDeadlineDay() {
        LocalDateTime now = LocalDate.now().atTime(16, 1);
        LocalDate responseDeadline = LocalDate.now();
        moreTimeRequestRule.assertIsNotPastDeadline(now, responseDeadline);
    }

}
