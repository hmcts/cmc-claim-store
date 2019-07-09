package uk.gov.hmcts.cmc.ccd_adapter.assertion;

import org.assertj.core.api.AbstractAssert;

import java.math.BigDecimal;
import java.util.Objects;

import static org.apache.commons.lang3.math.NumberUtils.createBigInteger;
import static uk.gov.hmcts.cmc.domain.utils.MonetaryConversions.poundsToPennies;

public class MoneyAssert extends AbstractAssert<MoneyAssert, BigDecimal> {

    public MoneyAssert(BigDecimal amountInPounds) {
        super(amountInPounds, MoneyAssert.class);
    }

    public MoneyAssert isEqualTo(String amountInPennies) {
        isNotNull();

        if (isValid(amountInPennies)) {
            failWithMessage("Expected amount to be <%s> but was <%s>",
                poundsToPennies(actual), amountInPennies);
        }

        return this;
    }

    public MoneyAssert isEqualTo(String amountInPennies, String errorMessage) {
        isNotNull();

        if (isValid(amountInPennies)) {
            failWithMessage(errorMessage);
        }

        return this;
    }

    private boolean isValid(String amountInPennies) {
        return !Objects.equals(poundsToPennies(actual), createBigInteger(amountInPennies));
    }

}
