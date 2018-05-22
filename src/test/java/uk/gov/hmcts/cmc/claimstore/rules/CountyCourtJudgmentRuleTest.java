package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CountyCourtJudgmentRuleTest {

    @Spy
    private ClaimDeadlineService claimDeadlineService = new ClaimDeadlineService();

    private CountyCourtJudgmentRule countyCourtJudgmentRule;

    @Before
    public void beforeEachTest() {
        countyCourtJudgmentRule = new CountyCourtJudgmentRule(claimDeadlineService);
    }

    @Test
    public void shouldNotThrowExceptionWhenCCJCanBeRequested() {
        Claim claim = SampleClaim.builder().withResponseDeadline(LocalDate.now().minusMonths(2)).build();
        countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(claim);
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowExceptionWhenClaimWasResponded() {
        Claim respondedClaim = SampleClaim.builder().withRespondedAt(now().minusDays(2)).build();
        countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(respondedClaim);
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowExceptionWhenUserCannotRequestCountyCourtJudgmentYet() {
        Claim claim = SampleClaim.getWithResponseDeadline(LocalDate.now().plusDays(12));
        countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(claim);
    }

    @Test
    public void shouldCallClaimDeadlineServiceToCheckIfJudgementCanBeRequested() {
        LocalDate deadlineDay = LocalDate.now().minusMonths(2);
        Claim claim = SampleClaim.builder().withResponseDeadline(deadlineDay).build();
        countyCourtJudgmentRule.assertCountyCourtJudgementCanBeRequested(claim);
        verify(claimDeadlineService).isPastDeadline(any(LocalDateTime.class), eq(deadlineDay));
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
