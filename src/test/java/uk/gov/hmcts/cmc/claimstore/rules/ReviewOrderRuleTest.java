package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleReviewOrder;

@RunWith(MockitoJUnitRunner.class)
public class ReviewOrderRuleTest {

    private final ReviewOrderRule reviewOrderRule = new ReviewOrderRule();

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenClaimIsNull() {
        reviewOrderRule.assertReviewOrder(null);
    }

    @Test(expected = ConflictException.class)
    public void shouldFailIfAlreadySubmittedReviewOrder() {
        Claim claim = SampleClaim.builder().withReviewOrder(SampleReviewOrder.getDefault()).build();
        reviewOrderRule.assertReviewOrder(claim);
    }

    @Test()
    public void shouldAllowForValidReviewOrder() {
        Claim claim = SampleClaim.getDefault();
        reviewOrderRule.assertReviewOrder(claim);
    }
}
