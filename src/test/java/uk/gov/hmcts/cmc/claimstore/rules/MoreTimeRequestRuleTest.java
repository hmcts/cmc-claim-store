package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeRequestedAfterDeadlineException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MoreTimeRequestRuleTest {

    @Spy
    private ClaimDeadlineService claimDeadlineService = new ClaimDeadlineService();

    private MoreTimeRequestRule moreTimeRequestRule;

    @Before
    public void beforeEachTest() {
        moreTimeRequestRule = new MoreTimeRequestRule(claimDeadlineService);
    }

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
    public void shouldCallClaimDeadlineServiceToCheckIfDeadlineHasPassed() {
        LocalDate deadlineDay = LocalDate.now().plusDays(2);
        Claim claim = SampleClaim.builder()
            .withResponseDeadline(deadlineDay)
            .withMoreTimeRequested(false)
            .build();
        moreTimeRequestRule.assertMoreTimeCanBeRequested(claim);
        verify(claimDeadlineService).isPastDeadline(any(LocalDateTime.class), eq(deadlineDay));
    }

}
