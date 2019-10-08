package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeRequestedAfterDeadlineException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@RunWith(MockitoJUnitRunner.class)
public class MoreTimeRequestRuleTest {

    @Captor
    private ArgumentCaptor<LocalDateTime> currentDateTime;

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
        assertThatCode(() -> moreTimeRequestRule.assertMoreTimeCanBeRequested(claim, claim.getResponseDeadline())).doesNotThrowAnyException();
    }

    @Test(expected = MoreTimeAlreadyRequestedException.class)
    public void shouldThrowExceptionWhenMoreTimeWasAlreadyRequested() {
        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().plusDays(2))
            .withMoreTimeRequested(true)
            .build();
        moreTimeRequestRule.assertMoreTimeCanBeRequested(claim, claim.getResponseDeadline());
    }

    @Test(expected = MoreTimeRequestedAfterDeadlineException.class)
    public void shouldThrowExceptionWhenResponseDeadlinePassed() {
        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusDays(1))
            .withMoreTimeRequested(false)
            .build();
        moreTimeRequestRule.assertMoreTimeCanBeRequested(claim, claim.getResponseDeadline());
    }

    @Test
    public void shouldCallClaimDeadlineServicePassingCurrentUKTimeToCheckIfDeadlineHasPassed() {
        LocalDate deadlineDay = LocalDate.now().plusDays(2);
        Claim claim = SampleClaim.builder()
            .withResponseDeadline(deadlineDay)
            .withMoreTimeRequested(false)
            .build();
        moreTimeRequestRule.assertMoreTimeCanBeRequested(claim, claim.getResponseDeadline());

        verify(claimDeadlineService).isPastDeadline(currentDateTime.capture(), eq(deadlineDay));
        assertThat(currentDateTime.getValue()).isCloseTo(nowInLocalZone(), within(10, ChronoUnit.SECONDS));
    }

}
