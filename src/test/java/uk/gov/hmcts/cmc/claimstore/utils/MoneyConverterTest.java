package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class MoneyConverterTest {

    @Test
    public void convertPoundsToPenniesForNullParameter() {
        assertThat(MoneyConverter.convertPoundsToPennies(null)).isNull();
    }

    @Test
    public void convertPoundsToPenniesForWholeNumber() {
        BigInteger pennies = MoneyConverter.convertPoundsToPennies(BigDecimal.valueOf(1));
        assertThat(pennies.intValue()).isEqualTo(100);
    }

    @Test
    public void convertPoundsToPenniesForFractionalAmount() {
        BigInteger pennies = MoneyConverter.convertPoundsToPennies(BigDecimal.valueOf(123.45));
        assertThat(pennies.intValue()).isEqualTo(12345);
    }
}
