package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MoreTimeRequestRuleTest {

    @Mock
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

    @Test
    public void noExceptionThrownWhenMoreTimeRequestedFirstTimeAndDeadlineHasPassed() {
        Claim claim = SampleClaim.builder()
                .withResponseDeadline(LocalDate.now().minusDays(2))
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

    @Test
    public void shouldReturnValidationErrorWhenAlreadyRequestedMoreTime() {
        LocalDate deadlineDay = LocalDate.now().plusDays(2);
        Claim claim = SampleClaim.builder()
                .withResponseDeadline(deadlineDay)
                .withMoreTimeRequested(true)
                .build();
        List<String> errors = moreTimeRequestRule.validateMoreTimeCanBeRequested(claim, deadlineDay);
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors).contains(moreTimeRequestRule.ALREADY_REQUESTED_MORE_TIME_ERROR);
    }

    @Test
    public void shouldReturnValidationErrorWhenAlreadyResponded() {
        LocalDate deadlineDay = LocalDate.now().plusDays(2);
        Claim claim = SampleClaim.builder()
                .withResponseDeadline(deadlineDay)
                .withMoreTimeRequested(false)
                .withRespondedAt(LocalDateTime.now().minusDays(2))
                .build();
        List<String> errors = moreTimeRequestRule.validateMoreTimeCanBeRequested(claim, deadlineDay);
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors).contains(moreTimeRequestRule.ALREADY_RESPONDED_ERROR);
    }

    @Test
    public void shouldReturnValidationErrorWhenPastDeadlineError() {
        LocalDate deadlineDay = LocalDate.now().minusDays(2);
        Claim claim = SampleClaim.builder()
                .withResponseDeadline(deadlineDay)
                .withMoreTimeRequested(false)
                .build();
        when(claimDeadlineService.isPastDeadline(any(), eq(deadlineDay))).thenReturn(true);
        List<String> errors = moreTimeRequestRule.validateMoreTimeCanBeRequested(claim, deadlineDay);
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors).contains(moreTimeRequestRule.PAST_DEADLINE_ERROR);
    }

}
