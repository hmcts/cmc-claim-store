package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmountRange;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;

import java.util.Objects;

public class AmountRangeAssert extends AbstractAssert<AmountRangeAssert, AmountRange> {

    public AmountRangeAssert(AmountRange actual) {
        super(actual, AmountRangeAssert.class);
    }

    public AmountRangeAssert isEqualTo(CCDAmountRange ccdAmountRange) {
        isNotNull();

        if (!Objects.equals(actual.getLowerValue().orElse(null), ccdAmountRange.getLowerValue())) {
            failWithMessage("Expected AmountRange.lowerValue to be <%s> but was <%s>",
                ccdAmountRange.getLowerValue(), actual.getLowerValue());
        }

        if (!Objects.equals(actual.getHigherValue(), ccdAmountRange.getHigherValue())) {
            failWithMessage("Expected AmountRange.higherValue to be <%s> but was <%s>",
                ccdAmountRange.getHigherValue(), actual.getHigherValue());
        }

        return this;
    }

}
