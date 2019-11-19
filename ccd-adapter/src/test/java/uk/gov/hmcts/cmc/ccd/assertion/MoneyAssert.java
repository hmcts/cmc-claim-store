package uk.gov.hmcts.cmc.ccd.assertion;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import static org.apache.commons.lang3.math.NumberUtils.createBigInteger;
import static uk.gov.hmcts.cmc.domain.utils.MonetaryConversions.poundsToPennies;

public class MoneyAssert extends CustomAssert<MoneyAssert, BigDecimal> {

    MoneyAssert(BigDecimal amountInPounds) {
        super("Amount", amountInPounds, MoneyAssert.class);
    }

    public MoneyAssert isEqualTo(String expected) {
        isNotNull();

        BigInteger expectedValue = createBigInteger(expected);
        BigInteger actualValue = poundsToPennies(actual);
        if (!Objects.equals(actualValue, expectedValue)) {
            failExpectedEqual("value", expectedValue, actualValue);
        }

        return this;
    }

}
