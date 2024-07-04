package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleReviewOrder;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ReviewOrderRuleTest {

    private final ReviewOrderRule reviewOrderRule = new ReviewOrderRule();

    @Test
    public void shouldThrowExceptionWhenClaimIsNull() {
        assertThrows(NullPointerException.class, () -> {
            reviewOrderRule.assertReviewOrder(null);
        });
    }

    @Test
    public void shouldFailIfAlreadySubmittedReviewOrder() {
        Claim claim = SampleClaim.builder().withReviewOrder(SampleReviewOrder.getDefault()).build();
        assertThrows(ConflictException.class, () -> {
            reviewOrderRule.assertReviewOrder(claim);
        });
    }

    @Test()
    public void shouldAllowForValidReviewOrder() {
        Claim claim = SampleClaim.getDefault();
        reviewOrderRule.assertReviewOrder(claim);
    }
}
