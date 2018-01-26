package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;

import java.time.LocalDate;

import static java.time.LocalDateTime.now;

public class CountyCourtJudgmentRuleTest {

    private final CountyCourtJudgmentRule countyCourtJudgmentRule = new CountyCourtJudgmentRule();

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
