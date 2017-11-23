package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmount;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.amount.NotKnown;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.AmountType.NOT_KNOWN;
import static uk.gov.hmcts.cmc.ccd.domain.AmountType.RANGE;

public class AmountAssert extends AbstractAssert<AmountAssert, Amount> {

    public AmountAssert(Amount amount) {
        super(amount, AmountAssert.class);
    }

    public AmountAssert isEqualTo(CCDAmount ccdAmount) {
        isNotNull();

        if (actual instanceof AmountRange) {
            if (!Objects.equals(RANGE, ccdAmount.getType())) {
                failWithMessage("Expected CCDAmount.type to be <%s> but was <%s>",
                    ccdAmount.getType(), RANGE);
            }
            assertThat((AmountRange) actual).isEqualTo(ccdAmount.getAmountRange());
        } else if (actual instanceof NotKnown) {
            if (!Objects.equals(NOT_KNOWN, ccdAmount.getType())) {
                failWithMessage("Expected CCDAmount.type to be <%s> but was <%s>",
                    ccdAmount.getType(), NOT_KNOWN);
            }
        }

        return this;
    }
}
