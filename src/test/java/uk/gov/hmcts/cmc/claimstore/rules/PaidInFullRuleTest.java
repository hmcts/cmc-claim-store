package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.ClaimantLinkException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static java.time.LocalDate.now;

@RunWith(MockitoJUnitRunner.class)
public class PaidInFullRuleTest {

    private final PaidInFullRule paidInFullRule = new PaidInFullRule();

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenClaimIsNull() {
        paidInFullRule.assertPaidInFull(null, "1");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenClaimantIdIsNull() {
        Claim claim = SampleClaim.getDefault();
        paidInFullRule.assertPaidInFull(claim, null);
    }

    @Test(expected = ClaimantLinkException.class)
    public void shouldThrowExceptionWhenClaimantDoesNotOwnTheCase() {
        Claim claim = SampleClaim.getDefault();
        paidInFullRule.assertPaidInFull(claim, "2");
    }

    @Test(expected = ConflictException.class)
    public void shouldFailIfAlreadySubmittedPaidInFull() {
        Claim claim = SampleClaim.builder().withMoneyReceivedOn(now()).build();
        paidInFullRule.assertPaidInFull(claim, claim.getSubmitterId());
    }

    @Test()
    public void shouldAllowForValidPaidInFull() {
        Claim claim = SampleClaim.getDefault();
        paidInFullRule.assertPaidInFull(claim, claim.getSubmitterId());
    }
}
