package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmount;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.amount.NotKnown;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.AmountType.NOT_KNOWN;

public class AmountAssert extends AbstractAssert<AmountAssert, Amount> {

    public AmountAssert(Amount amount) {
        super(amount, AmountAssert.class);
    }

    public AmountAssert isEqualTo(CCDAmount ccdAmount) {
        isNotNull();

        if (actual instanceof AmountRange) {
            assertThat((AmountRange) actual).isEqualTo(ccdAmount.getAmountRange());
        }

        if (actual instanceof NotKnown && NOT_KNOWN != ccdAmount.getType()) {
            failWithMessage("Expected CCDAmount.type to be <%s> but was <%s>",
                ccdAmount.getType(), NOT_KNOWN);
        }

        return this;
    }
}
