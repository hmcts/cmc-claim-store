package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDInterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;

import java.util.Objects;

public class InterestBreakdownAssert extends AbstractAssert<InterestBreakdownAssert, InterestBreakdown> {
    public InterestBreakdownAssert(InterestBreakdown actual) {
        super(actual, InterestBreakdownAssert.class);
    }

    public InterestBreakdownAssert isEqualTo(CCDInterestBreakdown other) {
        isNotNull();

        if (!Objects.equals(actual.getTotalAmount(), other.getTotalAmount())) {
            failWithMessage("Expected InterestBreakdown.totalAmount to be <%s> but was <%s>",
                other.getTotalAmount(), actual.getTotalAmount());
        }
        if (!Objects.equals(actual.getExplanation(), other.getExplanation())) {
            failWithMessage("Expected InterestBreakdown.explanation to be <%s> but was <%s>",
                other.getExplanation(), actual.getExplanation());
        }

        return this;
    }
}
