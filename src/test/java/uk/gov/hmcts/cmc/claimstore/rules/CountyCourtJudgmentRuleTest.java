package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CountyCourtJudgmentRuleTest {

    private final CountyCourtJudgmentRule countyCourtJudgmentRule = new CountyCourtJudgmentRule();

    @Test
    public void saveShouldFinishSuccessfullyForHappyPath() {
        Claim claim = SampleClaim.builder().withResponseDeadline(LocalDate.now().minusMonths(2)).build();
        countyCourtJudgmentRule.assertCanIssueCountyCourtJudgment(claim);
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowsForbiddenActionExceptionWhenClaimWasResponded() {
        Claim respondedClaim = SampleClaim.builder().withRespondedAt(LocalDateTime.now().minusDays(2)).build();
        countyCourtJudgmentRule.assertCanIssueCountyCourtJudgment(respondedClaim);
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowsForbiddenActionExceptionWhenUserCannotRequestCountyCourtJudgmentYet() {
        Claim claim = SampleClaim.getWithResponseDeadline(LocalDate.now().plusDays(12));
        countyCourtJudgmentRule.assertCanIssueCountyCourtJudgment(claim);
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowsForbiddenActionExceptionWhenCountyCourtJudgmentWasAlreadySubmitted() {
        Claim claim = SampleClaim.getDefault();
        countyCourtJudgmentRule.assertCanIssueCountyCourtJudgment(claim);
    }
}
