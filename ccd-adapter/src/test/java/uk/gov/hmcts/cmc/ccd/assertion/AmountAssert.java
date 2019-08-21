package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.AmountRow;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.amount.NotKnown;

import java.util.Objects;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class AmountAssert extends AbstractAssert<AmountAssert, Amount> {

    public AmountAssert(Amount amount) {
        super(amount, AmountAssert.class);
    }

    public AmountAssert isMappedTo(CCDCase ccdCase) {
        isNotNull();

        if (actual instanceof AmountBreakDown) {
            AmountBreakDown amountBreakDown = (AmountBreakDown) actual;

            AmountRow amountRow = amountBreakDown.getRows().get(0);
            CCDAmountRow ccdAmountRow = ccdCase.getAmountBreakDown().get(0).getValue();

            if (!Objects.equals(amountRow.getReason(), ccdAmountRow.getReason())) {
                failWithMessage("Expected CCDCase.amountRowReason to be <%s> but was <%s>",
                    ccdAmountRow.getReason(), amountRow.getReason());
            }

            String message = format("Expected CCDCase.amount to be <%s> but was <%s>",
                ccdAmountRow.getAmount(), amountRow.getAmount());
            assertMoney(amountRow.getAmount()).isEqualTo(ccdAmountRow.getAmount(), message);

        } else if (actual instanceof AmountRange) {

            AmountRange amountRange = (AmountRange) actual;
            String message = format("Expected CCDCase.amountHigherValue to be <%s> but was <%s>",
                ccdCase.getAmountHigherValue(), amountRange.getHigherValue());
            assertMoney(amountRange.getHigherValue()).isEqualTo(ccdCase.getAmountHigherValue(), message);

            amountRange.getLowerValue().ifPresent(lowerAmount -> {
                String errorMessage = format("Expected CCDCase.amountLowerValue to be <%s> but was <%s>",
                    ccdCase.getAmountHigherValue(), amountRange.getHigherValue());
                assertMoney(lowerAmount).isEqualTo(ccdCase.getAmountLowerValue(), errorMessage);
            });
        } else {
            assertThat(actual).isInstanceOf(NotKnown.class);
        }
        return this;
    }

}
