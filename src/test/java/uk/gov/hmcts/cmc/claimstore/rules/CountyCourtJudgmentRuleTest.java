package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@RunWith(MockitoJUnitRunner.class)
public class CountyCourtJudgmentRuleTest {

    @Captor
    private ArgumentCaptor<LocalDateTime> currentDateTime;

    @Spy
    private ClaimDeadlineService claimDeadlineService = new ClaimDeadlineService();

    private CountyCourtJudgmentRule countyCourtJudgmentRule;

    @Before
    public void beforeEachTest() {
        countyCourtJudgmentRule = new CountyCourtJudgmentRule(claimDeadlineService);
    }

    @Test
    public void shouldNotThrowExceptionWhenDeadlineWasYesterdayAndCCJCanBeRequested() {
        Claim claim = SampleClaim.builder().withResponseDeadline(LocalDate.now().minusDays(1)).build();
        assertThatCode(() ->
            countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(claim)
        ).doesNotThrowAnyException();
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowExceptionWhenUserCannotRequestCountyCourtJudgmentBecauseDeadlineIsTomorrow() {
        Claim claim = SampleClaim.getWithResponseDeadline(LocalDate.now().plusDays(1));
        countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(claim);
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowExceptionWhenClaimWasResponded() {
        Claim respondedClaim = SampleClaim.builder().withRespondedAt(now().minusDays(2)).build();
        countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(respondedClaim);
    }

    @Test
    public void shouldCallClaimDeadlineServicePassingCurrentUKTimeToCheckIfJudgementCanBeRequested() {
        LocalDate deadlineDay = LocalDate.now().minusMonths(2);
        Claim claim = SampleClaim.builder().withResponseDeadline(deadlineDay).build();
        countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(claim);

        verify(claimDeadlineService).isPastDeadline(currentDateTime.capture(), eq(deadlineDay));
        assertThat(currentDateTime.getValue()).isCloseTo(nowInLocalZone(), within(10, ChronoUnit.SECONDS));
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowExceptionWhenCountyCourtJudgmentWasAlreadySubmitted() {
        Claim claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(now())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .withPaymentOptionImmediately()
                    .build()
            ).build();
        countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(claim);
    }
}
