package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.exceptions.ClaimantLinkException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static java.time.LocalDate.now;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class PaidInFullRuleTest {

    private final PaidInFullRule paidInFullRule = new PaidInFullRule();

    @Test
    public void shouldThrowExceptionWhenClaimIsNull() {
        assertThrows(NullPointerException.class, () -> {
            paidInFullRule.assertPaidInFull(null, "1");
        });
    }

    @Test
    public void shouldThrowExceptionWhenClaimantIdIsNull() {
        Claim claim = SampleClaim.getDefault();
        assertThrows(NullPointerException.class, () -> {
            paidInFullRule.assertPaidInFull(claim, null);
        });
    }

    @Test
    public void shouldThrowExceptionWhenClaimantDoesNotOwnTheCase() {
        Claim claim = SampleClaim.getDefault();
        assertThrows(ClaimantLinkException.class, () -> {
            paidInFullRule.assertPaidInFull(claim, "2");
        });
    }

    @Test
    public void shouldFailIfAlreadySubmittedPaidInFull() {
        Claim claim = SampleClaim.builder().withMoneyReceivedOn(now()).build();
        assertThrows(ConflictException.class, () -> {
            paidInFullRule.assertPaidInFull(claim, claim.getSubmitterId());
        });
    }

    @Test()
    public void shouldAllowForValidPaidInFull() {
        Claim claim = SampleClaim.getDefault();
        paidInFullRule.assertPaidInFull(claim, claim.getSubmitterId());
    }
}
